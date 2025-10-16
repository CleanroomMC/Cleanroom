package com.cleanroommc.gradle.helpers

/**
 * @author ZZZank
 */
interface ProjectConstants {
    List<String> JVM_ARGUMENTS = [
        '--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED',
        '--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED',
        '--add-opens=java.base/java.lang=ALL-UNNAMED',
        '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED'
    ]

    List<String> COMPILER_JVM_ARGUMENTS = [
        '--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED',
        '--add-exports=java.base/jdk.internal.reflect=ALL-UNNAMED'
    ]

    Map<String, Object> PATCHER_POST_PROCESSOR = [
        tool: 'net.minecraftforge:mcpcleanup:2.3.2:fatjar',
        repo: 'https://maven.minecraftforge.net/',
        args: ['--input', '{input}', '--output', '{output}']
    ]
}