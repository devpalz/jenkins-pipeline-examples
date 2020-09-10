pipeline {
    agent {
        label 'dind'
    }
    stages {
        stage('Test') {
            steps {
                script{
                    docker.withServer('tcp://172.17.0.1:2376', 'DOCKER_HOST_CERTS'){
                        sh '''
                            # Get the SHA of the latest Jenkins Version
                            SHA=$(curl https://updates.jenkins-ci.org/download/war/ | tac | tac |  grep -m 2 '<tr>' | head -2 | grep -o "SHA-256: [a-zA-Z0-9]*" | sed 's/SHA-256: //g')
                            echo "Latest SHA is $SHA"
                            
                            # Get the latest Jenkins Version
                            VERSION=$(curl https://updates.jenkins-ci.org/download/war/ | tac | tac |  grep -m 2 '<tr>' | head -2 | grep -Eo '[0-9]+(\\.[0-9]+)' | head -1)
                            echo "Latest Version is $VERSION"
                            
                            # Build the Jenkins docker image from the Jenkins repo using the build arguments they expose
                            docker build \\
                               -t jenkins/jenkins \\
                              --build-arg uid=1500 \\
                              --build-arg gid=1500 \\
                              --build-arg JENKINS_VERSION=$VERSION \\
                              --build-arg JENKINS_SHA=$SHA \\
                              https://github.com/jenkinsci/docker.git#master

                        '''
                    }
                }
            }
        }
    }
}