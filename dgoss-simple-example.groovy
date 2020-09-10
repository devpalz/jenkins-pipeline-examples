pipeline {
    agent {
        label 'dind'
    }
    options{
        ansiColor('xterm')
    }
    parameters {
        string(name: 'BUILD_FOLDER', defaultValue: '', description: 'The build folder to use')
    }
    stages {
        stage('Build Docker Image'){
            steps{
                script{
                    dir(params.BUILD_FOLDER) {
                        docker.withServer('tcp://172.17.0.1:2376', 'DOCKER_HOST_CERTS') {
                            env.IMAGE_NAME = "mysql-init:${env.BUILD_ID}"
                            def image = docker.build(env.IMAGE_NAME)
                        }
                    }
                }
            }
        }
        stage('Test Docker Image'){
            steps{
                script{
                    dir(params.BUILD_FOLDER) {
                        docker.withServer('tcp://172.17.0.1:2376', 'DOCKER_HOST_CERTS') {
                            sh """ dgoss run -e \"MYSQL_RANDOM_ROOT_PASSWORD=true\" $env.IMAGE_NAME """
                        }
                    }
                }
            }
        }
        stage('Push Docker Image'){
            steps{
                script{

                    // If the branch is develop/master then use the tag latest
                    TAG = (env.BRANCH_NAME =~ "master|develop") ? "latest" : env.BRANCH_NAME

                    docker.withServer('tcp://172.17.0.1:2376', 'DOCKER_HOST_CERTS') {
                        docker.withRegistry('http://localhost:5000'){
                            docker.image(env.IMAGE_NAME).push()
                        }
                    }
                }
            }
        }
    }
}