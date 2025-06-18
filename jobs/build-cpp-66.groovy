pipeline {
    agent { label 'jenkins-agent' }  // Ensure this label matches your autoscaling group nodes

    environment {
        REPO_URL = 'https://github.com/PaulBarbat/Card_Game_66.git'
        BUILD_DIR = 'card_game_66_build'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Cloning C++ repository..."
                git branch: 'main', url: "${REPO_URL}"
            }
        }

        stage('Build') {
            steps {
                echo "Building the C++ project..."
                sh '''
                mkdir -p ${BUILD_DIR}
                cd ${BUILD_DIR}
                cmake ..
                make -j$(nproc)
                ls -ll
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
