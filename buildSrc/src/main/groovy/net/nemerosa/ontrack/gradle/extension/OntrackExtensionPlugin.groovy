package net.nemerosa.ontrack.gradle.extension

import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class OntrackExtensionPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println "[ontrack] Applying INTERNAL Ontrack plugin to ${project.path}"

        /**
         * Project's configuration
         */

        project.extensions.create('ontrack', OntrackExtension)

        /**
         * NPM setup
         */

        project.ext {
            nodeDir = project.file("${project.buildDir}")
        }

        project.apply plugin: 'com.moowork.node'
        project.node {

            version = '4.2.2'
            npmVersion = '2.14.7'
            download = true

            // Set the work directory for unpacking node
            workDir = project.file("${project.buildDir}/nodejs")

            // Set the work directory for NPM
            npmWorkDir = project.file("${project.buildDir}/npm")

            // Set the work directory where node_modules should be located
            nodeModulesDir = project.nodeDir
        }

        /**
         * NPM tasks
         */

        project.tasks.create('copyPackageJson') {
            // Without any input reference, the target file will never be replaced
            // outputs.file new File(project.nodeDir as String, 'package.json')
            doLast {
                println "[ontrack] Copies the package.json file to ${project.nodeDir}"
                project.mkdir new File(project.nodeDir as String)
                new File(project.nodeDir as String, 'package.json').text = getClass().getResourceAsStream('/extension/package.json').text
            }
        }

        project.tasks.create('copyGulpFile') {
            // Without any input reference, the target file will never be replaced
            // outputs.file new File(project.nodeDir as String, 'gulpfile.js')
            doLast {
                println "[ontrack] Copies the gulpfile.js file to ${project.nodeDir}"
                project.mkdir new File(project.nodeDir as String)
                new File(project.nodeDir as String, 'gulpfile.js').text = getClass().getResourceAsStream('/extension/gulpfile.js').text
            }
        }

        project.tasks.npmInstall.dependsOn('copyPackageJson')

        /**
         * Gulp call
         */

        project.tasks.create('web', NodeTask) {
            dependsOn 'npmInstall'
            dependsOn 'copyGulpFile'

            inputs.dir project.file('src/main/resources/static')
            outputs.file project.file('build/web/dist/module.js')

            doFirst {
                project.mkdir project.buildDir
                println "[ontrack] Generating web resources of ${project.extensions.ontrack.id(project)} in ${project.buildDir}"
            }

            execOverrides {
                it.workingDir = new File(project.nodeDir as String)
            }

            script = new File(new File(project.nodeDir, 'node_modules'), 'gulp/bin/gulp')
            args = [
                    'default',
                    '--extension', project.extensions.ontrack.id(project),
                    '--version', project.version,
                    '--src', project.file('src/main/resources/static'),
                    '--target', project.buildDir
            ]
        }

        /**
         * Version file
         */

        project.tasks.create('ontrackProperties') {
            description "Prepares the ontrack META-INF file"
            doLast {
                project.file("build/ontrack.properties").text = """\
# Ontrack extension properties
version = ${project.version}
"""
            }
        }

        /**
         * Update of the JAR task
         */

        project.tasks.jar {
            dependsOn 'web'
            dependsOn 'ontrackProperties'
            from('build/web/dist') {
                into { "static/extension/${project.extensions.ontrack.id(project)}/${project.version}/" }
            }
            from('build') {
                include 'ontrack.properties'
                into "META-INF/ontrack/extension/"
                rename { "${project.extensions.ontrack.id(project)}.properties" }
            }
            exclude 'static/**/*.js'
            exclude 'static/**/*.html'
        }

    }

}
