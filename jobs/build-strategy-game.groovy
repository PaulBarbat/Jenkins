pipeline {
    parameters{
        string(name: 'BRANCH' , defaultValue: 'main', description: 'git branch to build')
        booleanParam(name:'RUN_TESTS', defaultValue: false, description:'Run test stage?')
    }
    agent {label 'jenkins-agent'}

    environment{
        REPO_URL = 'https://github.com/PaulBarbat/StrategyGameEngine.git'
        BUILD_DIR = 'build'
    }

    stages{
        stage('Checkout'){
            steps{
                echo "Cloning StrategyGameEngine repository..."
                git branch: "${params.BRANCH}", url: "${REPO_URL}"
            }
        }

        stage('Build Linux'){
            echo "Building for Linux"
            sh '''
                mkdir -p ${BUILD_DIR}/linux
                mkdir -p ${BUILD_DIR}/linux/resources
                cp -r resources/* ${BUILD_DIR}/linux/resources
                cd ${BUILD_DIR}/linux
                cmake ../.. -G Ninja -DCMAKE_BUILD_TYPE=Release -DPLATFORM_NAME=Linux
                cmake --build .
                ls -ll
            '''
        }

        stage('Build Windows'){

        }

        stage('Tests'){

        }

        stage('Upload to S3'){

        }
    }

    post{
        success{
            echo "Build completed successfully"
        }
        failure{
            echo "Build failed!"
        }
    }
}