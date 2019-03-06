def label = "demo-pic-worker-${UUID.randomUUID().toString()}"

podTemplate(
    label: label,
    containers: [
        containerTemplate(
            name: 'jnlp',
            image: 'jenkins/jnlp-slave:3.10-1',
            args: '${computer.jnlpmac} ${computer.name}'
        ),
        containerTemplate(
            name: 'docker',
            image: 'docker:stable',
            command:'cat',
            ttyEnabled:true,
            envVars: [
                envVar(key: 'DOCKER_CONFIG', value: '/root/.docker')
            ]
        ),
        containerTemplate(
            name: 'kubectl',
            image: 'dtzar/helm-kubectl:2.13.0',
            command:'cat',
            ttyEnabled:true,
            envVars: [
                envVar(key: 'KUBECONFIG', value: '/root/.kube/config')
            ]
        ),
        containerTemplate(
            name: 'maven',
            image: 'maven:3.6.0-jdk-8-alpine',
            command:'cat',
            ttyEnabled:true
        )
    ],
    volumes: [
        hostPathVolume(mountPath: '/var/run/docker.sock', hostPath: '/var/run/docker.sock'),
        secretVolume(secretName: 'docker-config', mountPath: '/root/.docker'),
        secretVolume(secretName: 'kube-config', mountPath: '/root/.kube'),
        persistentVolumeClaim(mountPath: '/share', claimName: 'jenkins-slave-share'),
        configMapVolume(mountPath: '/config', configMapName: 'hello-spring-boot-job-config'),
        persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'jenkins-maven-m2'),
        secretVolume(secretName: 'maven-config', mountPath: '/usr/share/maven/ref')
    ]
)

{
    node(label){

        stage('Git clone'){
           container('jnlp'){
                git branch: 'master', url: 'https://github.com/seblaporte/hello-spring-boot.git'
           }
        }

        stage('Create image name'){
            container('jnlp'){
                sh  '''
                git rev-parse --short HEAD > /share/buildVersion
                echo "`cat /config/registryHost`/`cat /config/applicationName`:`cat /share/buildVersion`" > /share/imageName
                '''
            }
        }

        stage('Maven build'){
            container('maven'){
                sh 'mvn -s /usr/share/maven/ref/settings.xml clean package'
            }
        }

        stage('Deploy artifact to Nexus'){
            container('maven'){
                sh 'mvn -s /usr/share/maven/ref/settings.xml deploy:deploy'
            }
        }

    }

}