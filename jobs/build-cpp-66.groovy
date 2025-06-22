pipeline {
    agent { label 'jenkins-agent' }  // Ensure this label matches your autoscaling group nodes

    environment {
        REPO_URL = 'https://github.com/PaulBarbat/Card_Game_66.git'
        BUILD_DIR = 'build'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Cloning C++ repository..."
                git branch: 'main', url: "${REPO_URL}"
            }
        }

        stage('Build Linux') {
            steps {
                echo "Building for Linux"
                sh '''
                mkdir -p ${BUILD_DIR}/linux
                cd ${BUILD_DIR}/linux
                cmake ../.. -G Ninja -DCMAKE_BUILD_TYPE=Release -DPLATFORM_NAME=Linux
                cmake --build .
                ls -ll
                '''
            }
        }
        stage('Build Windows'){
            steps{
                echo "Build for Windows"
                sh '''
                mkdir -p build/windows
                cd build/windows
                cmake ../.. -G Ninja -DCMAKE_BUILD_TYPE=Release -DCMAKE_TOOLCHAIN_FILE=../../Toolchain_Windows.cmake -DPLATFORM_NAME=Windows
                cmake --build .
                '''
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh '''
                cd ${BUILD_DIR}
                ctest --output-on-failure
                '''
            }
        }

        stage('Upload to S3'){
            steps{
                script{
                    def version = readFile('Version.txt').trim()
                    def zipName = "Card_Game_66-${version}-${env.BUILD_NUMBER}.zip"

                    sh """
                        pwd
                        ls -ll
                        mkdir -p package_output
                        cp build/linux/Card_Game_66* package_output/
                        cp build/windows/Card_Game_66* package_output/
                        cp -r resources/*.xml package_output/resources/
                        ls -ll build/linux
                        ls -ll build/windows
                        cd package_output
                        zip -r ../${zipName} .
                    """

                    withAWS(role: 'arn:aws:iam::396913703657:role/jenkins-ec2-role-terraform', roleSessionName: 'jenkins-session') {
                        sh """
                            ls -ll
                            pwd
                            aws s3 cp ${zipName} s3://card-game-66-personal-bucket/builds/${zipName}
                        """
                    }
                }
                
            }
        }
    }

    post {
        success {
            echo "Build and tests completed successfully!"
        }
        failure {
            echo "Build failed! Check logs for details."
        }
    }
}
