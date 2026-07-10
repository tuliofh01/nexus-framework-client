pipeline {
    agent any

    parameters {
        string(name: 'PROJECT_NAME', defaultValue: 'MyApp', description: 'Generated project name')
        choice(name: 'TEMPLATE_TYPE', choices: ['desktop', 'android'], description: 'Template type')
        string(name: 'OUTPUT_DIR', defaultValue: 'builds/framework', description: 'Parent output directory')
        booleanParam(name: 'RUN_CMAKE_BUILD', defaultValue: false, description: 'Configure desktop template with CMake (desktop only)')
        booleanParam(name: 'DRY_RUN', defaultValue: false, description: 'Dry-run generation (no files written)')
    }

    environment {
        JAVA_HOME = tool name: 'jdk-26', type: 'jdk'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compile') {
            steps {
                sh './gradlew :core:compileKotlin :cli:compileKotlin --no-daemon'
            }
        }

        stage('Generate') {
            steps {
                script {
                    def dryRunFlag = params.DRY_RUN ? '--dry-run' : ''
                    sh """
                        ./gradlew :cli:run --no-daemon --args="generate \
                          --type ${params.TEMPLATE_TYPE} \
                          --name ${params.PROJECT_NAME} \
                          --output ${params.OUTPUT_DIR} \
                          ${dryRunFlag}"
                    """
                }
            }
        }

        stage('CMake build (optional)') {
            when {
                allOf {
                    expression { params.TEMPLATE_TYPE == 'desktop' }
                    expression { params.RUN_CMAKE_BUILD }
                    expression { !params.DRY_RUN }
                }
            }
            steps {
                dir("${params.OUTPUT_DIR}/${params.PROJECT_NAME}") {
                    sh 'cmake --preset debug || cmake -G Ninja -B builds/debug -DCMAKE_BUILD_TYPE=Debug .'
                    sh 'cmake --build builds/debug || cmake --build builds/debug'
                }
            }
        }
    }

    post {
        success {
            echo "Generation complete: ${params.OUTPUT_DIR}/${params.PROJECT_NAME}"
        }
        failure {
            echo 'Pipeline failed — see stage logs above.'
        }
    }
}
