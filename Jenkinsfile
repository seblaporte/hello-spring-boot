def label = "demo-pic-worker-${UUID.randomUUID().toString()}"

podTemplate(label: label, yaml: """

kind: Pod
metadata:
  name: jnlp-kaniko-maven-kubectl
spec:
  containers:
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    imagePullPolicy: Always
    command:
    - /busybox/cat
    tty: true
    volumeMounts:
      - name: docker-config
        mountPath: /kaniko/.docker/

  - name: jnlp
    image: jenkins/jnlp-slave:3.10-1
    imagePullPolicy: Always

  - name: maven
    image: maven:3.6.0-jdk-8-alpine
    imagePullPolicy: Always
    command:
    - cat
    tty: true
    volumeMounts:
      - name: maven-config
        mountPath: /usr/share/maven/ref
      - name: jenkins-maven-m2
        mountPath: /root/.m2

  - name: kubectl
    image: dtzar/helm-kubectl:2.13.0
    imagePullPolicy: Always
    tty: true
    volumeMounts:
      - name: kube-config
        mountPath: /root/.kube
    env:
    - name: KUBECONFIG
      value: "/root/.kube/config"

  - name: sonar-scanner
    image: newtmitch/sonar-scanner:3.2.0-alpine
    imagePullPolicy: Always
    tty: true
    command:
    - cat
    volumeMounts:
    - name: sonar-scanner-config
      mountPath: /root/sonar-scanner/conf/

  volumes:
  - name: docker-config
    secret:
      secretName: docker-config
  - name: kube-config
    secret:
      secretName: kube-config
  - name: maven-config
    secret:
      secretName: maven-config
  - name: jenkins-maven-m2
    persistentVolumeClaim:
      claimName: jenkins-maven-m2
  - name: sonar-scanner-config
    secret:
      secretName: sonar-scanner-config

"""
)
{
    node(label){

        stage('Get sources'){
           container('jnlp'){
                git branch: 'master', url: 'https://github.com/seblaporte/hello-spring-boot.git'
           }
        }

        stage('Build with Maven'){
            container('maven'){
                sh 'mvn -s /usr/share/maven/ref/settings.xml clean package -DskipTests'
            }
        }

        stage('Push artifact to Nexus'){
            container('maven'){
                sh 'mvn -s /usr/share/maven/ref/settings.xml deploy -DskipTests'
            }
        }

        stage('Build image and push to Docker registry'){
            container(name: 'kaniko', shell: '/busybox/sh') {
               withEnv(['PATH+EXTRA=/busybox:/kaniko']) {
                 sh '''#!/busybox/sh
                 /kaniko/executor -f `pwd`/Dockerfile -c `pwd` --cache=true --destination=registry.demo-pic.techlead-top.ovh/hello-spring-boot:latest
                 '''
               }
            }
        }

        stage('Deploy to integration'){
            container('kubectl'){
                sh '''
                kubectl patch deployment hello-spring-boot -p \
                  '{"spec":{"template":{"metadata":{"labels":{"date":"'`date +'%s'`'"}}}}}'
                '''
            }
        }

        stage('Sonar analysis'){
            container('sonar-scanner'){
                sh 'sonar-scanner'
            }
        }

    }

}
