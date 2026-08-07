package com.cleanroommc.gradle.helpers.tasks

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CreateMMCPackTask extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getVersionJson()

    @InputDirectory
    abstract DirectoryProperty getTemplateDir()

    @InputFile
    abstract RegularFileProperty getUniversalJarFile()

    @Input
    abstract Property<String> getCleanroomVersion()

    @Input
    abstract Property<Boolean> getIsRelease()

    @OutputFile
    abstract RegularFileProperty getOutputZip()

    @TaskAction
    void create() {
        def versionJsonFile = getVersionJson().get().asFile
        def templateDir = getTemplateDir().get().asFile
        def jarFile = getUniversalJarFile().get().asFile
        def version = getCleanroomVersion().get()
        def isRelease = getIsRelease().get()

        def outputDir = new File(getTemporaryDir(), 'mmc-output')
        outputDir.deleteDir()
        outputDir.mkdirs()

        project.copy {
            from(templateDir) {
                exclude 'patches/net.minecraft.json'
            }
            into outputDir
        }
        project.copy {
            from new File(templateDir, 'patches/net.minecraft.json')
            into new File(outputDir, 'patches')
        }

        def versionJson = new JsonSlurper().parse(versionJsonFile)
        def libraries = versionJson.libraries
        def mainClass = versionJson.mainClass
        def lwjglVersion = ''
        def forgeLibraries = []
        def lwjglLibraries = []

        libraries.each { lib ->
            def libName = lib.name as String
            if (libName.contains('org.lwjgl:')) {
                if (!lwjglVersion) {
                    lwjglVersion = libName.split(':')[2]
                }
                if (libName.contains('-arm64')) {
                    def rules = lib.rules
                    if (rules && rules[0] && rules[0].os) {
                        def arm64Rule = [action: 'allow', os: [name: rules[0].os.name + '-arm64']]
                        ((List) rules).add(arm64Rule)
                    }
                }
                lwjglLibraries.add(lib)
            } else {
                def parts = libName.split(':')
                if (parts.length >= 2 && parts[1] == 'cleanroom') {
                    if (isRelease) {
                        lib.downloads.artifact.url = "https://repo.cleanroommc.com/releases/com/cleanroommc/cleanroom/${version}/cleanroom-${version}-universal.jar"
                        forgeLibraries.add(lib)
                    } else {
                        forgeLibraries.add([
                            name: "com.cleanroommc:cleanroom:${version}-universal",
                            'MMC-hint': 'local'
                        ])
                    }
                } else {
                    forgeLibraries.add(lib)
                }
            }
        }

        def forgePatchTemplate = new JsonSlurper().parse(new File(templateDir, 'patches/net.minecraftforge.json'))
        forgePatchTemplate.libraries = forgeLibraries
        forgePatchTemplate.mainClass = mainClass
        forgePatchTemplate.version = version
        new File(outputDir, 'patches/net.minecraftforge.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(forgePatchTemplate))

        def lwjglPatchTemplate = new JsonSlurper().parse(new File(templateDir, 'patches/org.lwjgl3.json'))
        lwjglPatchTemplate.libraries = lwjglLibraries
        lwjglPatchTemplate.version = lwjglVersion
        new File(outputDir, 'patches/org.lwjgl3.json').text = JsonOutput.prettyPrint(JsonOutput.toJson(lwjglPatchTemplate))

        def mmcPackFile = new File(outputDir, 'mmc-pack.json')
        def mmcPack = new JsonSlurper().parse(mmcPackFile)
        mmcPack.components.each { component ->
            if (component.uid == 'org.lwjgl3') {
                component.version = lwjglVersion
                component.cachedVersion = lwjglVersion
            }
            if (component.uid == 'net.minecraft' && component.cachedRequires) {
                component.cachedRequires[0].suggests = lwjglVersion
            }
            if (component.uid == 'net.minecraftforge') {
                component.version = version
                component.cachedVersion = version
            }
        }
        mmcPackFile.text = JsonOutput.prettyPrint(JsonOutput.toJson(mmcPack))

        if (!isRelease) {
            def libsDir = new File(outputDir, 'libraries')
            libsDir.mkdirs()
            project.copy {
                from jarFile
                into libsDir
                rename { "cleanroom-${version}-universal.jar" }
            }
        }

        def zipFile = getOutputZip().get().asFile
        zipFile.parentFile.mkdirs()
        project.ant.zip(destfile: zipFile.absolutePath, basedir: outputDir.absolutePath)
    }
}
