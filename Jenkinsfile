pipeline {
    agent any

    stages {
      stage('Checkout') {
          steps {
              echo 'Checking out...'
              git credentialsId: 'silv3rio-bitbucket', url: 'https://github.com/silv3rio/todd/'
          }
      }
      stage('Build') {
          steps {
              echo 'Building...'
              sh './gradlew clean build'
          }
      }
      stage('Jenkins Archiving') {
          steps {
              echo 'Jenkins Archiving...'
              archiveArtifacts 'build/libs/*'
          }
      }
      stage ('Nexus Archiving') {
          steps {
            echo 'Nexus Archiving...'
              sh './gradlew uploadArchives'
          }
      }
      stage ('Launching ansible playbook mate!!!') {
          steps {
            echo 'siiiiiiiiiiga...'
            ansiblePlaybook playbook: '/var/jenkins_home/my_playbook.yml',inventory: '/var/jenkins_home/hosts',credentialsId: 'foo',sudo:'true'
          }
      }
    }
}
