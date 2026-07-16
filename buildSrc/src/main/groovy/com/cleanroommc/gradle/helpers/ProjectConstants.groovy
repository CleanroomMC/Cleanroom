package com.cleanroommc.gradle.helpers

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
}