package com.cleanroommc.gradle.helpers.tasks

import com.palantir.gradle.gitversion.VersionDetails;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;


abstract class GenVersionTask extends DefaultTask {
    @Input
    final String lastTag = ((Closure<VersionDetails>)getProject().rootProject.getExtensions().getExtraProperties().get('versionDetails')).call().lastTag
    @InputFile
    final File template = new File("${getProject().rootProject.projectDir}/templates/CleanroomVersion.java")
    @OutputFile
    final File versionClass = new File("${getProject().rootProject.projectDir}/src/main/java/com/cleanroommc/common/CleanroomVersion.java")
    @TaskAction
    void action() {
        versionClass.withWriter { def writer ->
            template.eachLine { def line ->
                def newLine = line.replace("%VERSION%", lastTag)
                    .replace("%BUILD_VERSION%", getProject().rootProject.version.toString())
                writer.write(newLine + "\n")
            }
        }
    }
}
