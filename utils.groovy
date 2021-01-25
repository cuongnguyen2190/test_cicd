import groovy.transform.Field

@Field final String credentialsId = '9fa16e4b-c0f9-4ff7-b90d-413021959382'


def git(args) {
	print("in the checkout ${args}")
	branch = args.branch ?: env.BRANCH
	url = args.url
	updateDescription = args.updateDescription ?: false
	poll = args.poll ?: false
	
	/**
     *  function for checkout
     */
    checkout(changelog: updateDescription, poll: poll,
            scm: [$class                           : 'GitSCM', branches: [[name: branch]],
                  doGenerateSubmoduleConfigurations: false,
                  extensions                       : [[$class: 'RelativeTargetDirectory'],
                                                      [$class : 'ChangelogToBranch',
                                                       options: [compareRemote: 'origin', compareTarget: 'master']]],
                  submoduleCfg                     : [],
                  userRemoteConfigs                : [[credentialsId: credentialsId,
                                                       url          : url]]
            ])

    def branchName = branch
    if (updateDescription == true) {
        branchName = sh(returnStdout: true, script: 'git describe --all').trim()
        branchCommit = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
        //    https://issues.jenkins-ci.org/browse/JENKINS-35230?focusedCommentId=270925
        currentBuild.displayName = "${currentBuild.displayName} [${branchName}]"
        if (currentBuild.description == null) {
            currentBuild.description = ""
        }
        currentBuild.description = "${currentBuild.description} last commit: ${branchCommit}"
    }

    return branchName
}