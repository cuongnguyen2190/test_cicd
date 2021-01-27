def set_params(poll) {
    def _parameters = [buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '15', numToKeepStr: '100')),
                       [$class         : 'RebuildSettings',
                        autoRebuild    : false,
                        rebuildDisabled: false],

                       [$class                       : 'ThrottleJobProperty',
                        categories                   : [],
                        limitOneJobWithMatchingParams: false,
                        maxConcurrentPerNode         : 0,
                        maxConcurrentTotal           : 0,
                        paramsToUseForLimit          : '',
                        throttleEnabled              : false,
                        throttleOption               : 'project']]
    if (!poll) {
        _parameters += parameters([string(defaultValue: 'test_auto_deploy', description: 'branch to deploy', name: 'branch'),])
		} else {
        _parameters += pipelineTriggers([pollSCM('* * * * *')])
    }
    properties(_parameters)
}


def deploy(branch) {
        def final_status_code = 0
        stage("Checkout source code") {
            dir("/home/dev/docker/ui") {
                def result = sh script:"""
                    git reset --hard HEAD && git checkout \${branch}  && git pull
                """, returnStatus:true
                final_status_code += result
            }
        }
        stage("Build and deploy image") {
            dir("/home/dev/docker/ui") {
                def result = sh script:"""
                    docker-compose up -d --build
                """, returnStatus:true
                final_status_code += result
            }
        }
        stage("Remove old image") {
            def result = sh script:"""
                yes y | docker system prune
            """, returnStatus:true
            final_status_code += result
        }
        return final_status_code
}

def deploy() {
    def poll = true
    // if (branch != null && branch.contains('master')) {
    //     poll = true
    // }
    print("tessssssss -------------------")
    set_params(poll)
    // utils = load "utils.groovy"
    print("tessssssss -------------------")
    node("PC") {
        exit_code = deploy("master")
        if (exit_code > 0) {
            currentBuild.result = "FAILURE"
        }
    }
}

return this
