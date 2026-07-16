package com.cleanroommc.gradle.helpers.tasks

class Util {

	static def getCurrentArch() {
		def osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("linux")) {
			def osArch = System.getProperty("os.arch")
			return osArch.startsWith("arm") || osArch.startsWith("aarch64")
					? "natives-linux-${osArch.contains("64") || osArch.startsWith("armv8") ? "arm64" : "arm32"}"
					: "natives-linux"
		}
		if (osName.contains("mac os x") || osName.contains("darwin") || osName.contains("osx")) {
			return System.getProperty("os.arch").startsWith("aarch64") ? "natives-macos-arm64" : "natives-macos"
		}
		if (osName.contains("windows")) {
			def osArch = System.getProperty("os.arch")
			return osArch.contains("64")
					? "natives-windows${osArch.startsWith("aarch64") ? "-arm64" : ""}"
					: "natives-windows-x86"
		}
	}

}
