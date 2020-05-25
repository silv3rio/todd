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
            ansiblePlaybook credentialsId: 'private_key', inventory: 'inventories/a/hosts', playbook: '/root/my_playbook.yml'
          }
      }
    }
}
