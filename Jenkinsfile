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

  - name: klar-scanner
    image: registry.demo-pic.techlead-top.ovh/klar
    imagePullPolicy: Always
    tty: true
    command:
    - cat
    env:
    - name: CLAIR_ADDR
      valueFrom:
        secretKeyRef:
          name: clair-config
          key: clairAddress
    - name: DOCKER_USER
      valueFrom:
        secretKeyRef:
          name: clair-config
          key: dockerUser
    - name: DOCKER_PASSWORD
      valueFrom:
        secretKeyRef:
          name: clair-config
          key: dockerPassword
    - name: CLAIR_OUTPUT
      value: High
    - name: CLAIR_THRESHOLD
      value: 10

  imagePullSecrets:
  - name: docker-registry-config

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
                git branch: BRANCH_NAME, url: 'https://github.com/seblaporte/hello-spring-boot.git'
           }
        }

        stage('Build with Maven'){
            container('maven'){
                sh 'mvn -s /usr/share/maven/ref/settings.xml clean package -DskipTests'
            }
        }

        stage('Build image and push to Docker registry'){
            container(name: 'kaniko', shell: '/busybox/sh') {
               withEnv(['PATH+EXTRA=/busybox:/kaniko']) {
                 sh '''#!/busybox/sh
                 /kaniko/executor -f `pwd`/Dockerfile -c `pwd` --cache=true --destination=registry.demo-pic.techlead-top.ovh/hello-spring-boot:$BRANCH_NAME
                 '''
               }
            }
        }

        stage('Deploy to integration'){
            container('kubectl'){
                sh '''
                cat <<EOF | kubectl apply -f -
                apiVersion: apps/v1
                kind: Deployment
                metadata:
                  labels:
                    app: hello-spring-boot
                  name: hello-spring-boot-$BRANCH_NAME
                  namespace: demo-pic
                spec:
                  replicas: 1
                  revisionHistoryLimit: 3
                  selector:
                    matchLabels:
                      app: hello-spring-boot
                  template:
                    metadata:
                      labels:
                        app: hello-spring-boot
                    spec:
                      containers:
                      - name: hello-spring-boot
                        image: registry.demo-pic.techlead-top.ovh/hello-spring-boot:$BRANCH_NAME
                        imagePullPolicy: Always
                        ports:
                          - name: web
                            containerPort: 8080
                      imagePullSecrets:
                        - name: docker-registry-config
                EOF
                '''
            }
        }

    }

}
