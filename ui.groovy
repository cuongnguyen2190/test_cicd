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
        // def final_status_code = 0
        stages {
            stage("Checkout source code") {
                dir("/home/dev/docker/ui") {
                    sh"""
                        git reset --hard HEAD && git checkout \${branch}  && git pull
                    """
                    // final_status_code += result.getStatus()
                    // print(result.getStdout())
                }
            }
            stage("Build and deploy image") {
                dir("/home/dev/docker/ui") {
                    sh"""
                        docker-compose up -d --build
                    """
                    // final_status_code += result.getStatus()
                    // print(result.getStdout())
                }
            }
            stage("Remove old image") {
                sh"""
                    yes y | docker system prune
                """
                // final_status_code += result.getStatus()
                // print(result.getStdout())
            }
        }
        // return final_status_code
}

def start() {
    def poll = true
    // if (branch != null && branch.contains('master')) {
    //     poll = true
    // }
    print(branch)
    // set_params(poll)
    utils = load "utils.groovy"
    print("OK")
    deploy(branch)
    if (exit_code > 0) {
        currentBuild.result = 'FAILURE'
    }
}

return this