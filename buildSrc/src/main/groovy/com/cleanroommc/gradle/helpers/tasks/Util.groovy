package com.cleanroommc.gradle.helpers.tasks

import groovy.json.JsonSlurper
import org.gradle.api.GradleException

import java.security.MessageDigest
import java.text.SimpleDateFormat

class Util {
	static void init() {
		File.metaClass.sha1 = { ->
			MessageDigest md = MessageDigest.getInstance('SHA-1')
			delegate.eachByte 4096, { bytes, size ->
				md.update(bytes, 0, size)
			}
			return md.digest().collect {String.format "%02x", it}.join()
		}
	
		File.metaClass.json = { -> new JsonSlurper().parseText(delegate.text) }
		
		Date.metaClass.iso8601 = { ->
			def format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
			def result = format.format(delegate)
			return result[0..21] + ':' + result[22..-1]
		}
        
        String.metaClass.rsplit = { String del, int limit = -1 ->
            def lst = new ArrayList()
            def x = 0, idx
            def tmp = delegate
            while ((idx = tmp.lastIndexOf(del)) != -1 && (limit == -1 || x++ < limit)) {
                lst.add(0, tmp.substring(idx + del.length(), tmp.length()))
                tmp = tmp.substring(0, idx)
            }
            lst.add(0, tmp)
            return lst
        }
	}
	
	static String[] getClasspath(project, libs, artifact) {
		def ret = []
		artifactTree(project, artifact).each { key, lib ->
			libs[lib.name] = lib
			if (lib.name != artifact)
				ret.add(lib.name)
		}
		return ret
	}
	
	static def getArtifacts(project, config, classifiers) {
		def ret = [:]
		config.resolvedConfiguration.resolvedArtifacts.each {
			def art = [
				group: it.moduleVersion.id.group,
				name: it.moduleVersion.id.name,
				version: it.moduleVersion.id.version,
				classifier: it.classifier,
				extension: it.extension,
				file: it.file
			]
			def key = art.group + ':' + art.name
			def folder = "${art.group.replace('.', '/')}/${art.name}/${art.version}/"
			def filename = "${art.name}-${art.version}"
			if (art.classifier != null)
				filename += "-${art.classifier}"
			filename += ".${art.extension}"
			def path = "${folder}${filename}"
			def url = getNativeURL(path, filename)
			//TODO remove when Mojang launcher is updated
			if (!classifiers && art.classifier != null) { 
				// Mojang launcher doesn't currently support classifiers, so... move it to part of the version, and force the extension to 'jar'
				// However, keep the path normal so that our mirror system works.
				art.version = "${art.version}-${art.classifier}"
				art.classifier = null
				art.extension = 'jar'
				path = "${art.group.replace('.', '/')}/${art.name}/${art.version}/${art.name}-${art.version}.jar"
			}
			ret[key] = [
				name: "${art.group}:${art.name}:${art.version}" + (art.classifier == null ? '' : ":${art.classifier}") + (art.extension == 'jar' ? '' : "@${art.extension}"),
				downloads: [
					artifact: [
						path: path,
						url: url,
						sha1: sha1(art.file),
						size: art.file.length()
					]
				]
			]
		}
		return ret
	}

	static def getLWJGLNatives(nativeConfig, compileConfig, natives, arch) {
		def ret = [:]
		def data = [ '' : [:]]
		nativeConfig.resolvedConfiguration.resolvedArtifacts.each {
			data.putIfAbsent(it.moduleVersion.id.name, [
					group    : it.moduleVersion.id.group,
					version  : it.moduleVersion.id.version,
					extension: it.extension,
			])
		}
		compileConfig.files.each {file ->
			try {
				def name = natives.stream().filter(it -> file.getName().contains(it)).findFirst().get()
				def classifier = arch.stream().filter(it -> file.getName().contains(it)).findFirst().get()
				def art = [
						name		  : name,
						group     : data.get(name).get("group"),
						version   : data.get(name).get("version"),
						extension : data.get(name).get("extension"),
						classifier: classifier
				]
				def key = art.group + ':' + art.name + ':' + art.classifier
				def folder = "${art.group.replace('.', '/')}/${art.name}/${art.version}/"
				def filename = "${art.name}-${art.version}"
				if (art.classifier != null)
					filename += "-${art.classifier}"
				filename += ".${art.extension}"
				def path = "${folder}${filename}"
				def url = getNativeURL(path, filename)
				ret[key] = [
						name: "${art.group}:${art.name}:${art.version}" + ":${art.classifier}" + (art.extension == 'jar' ? '' : "@${art.extension}"),
						downloads: [
								artifact: [
										path: path,
										url : url,
										sha1: sha1(file),
										size: file.length()
								]
						],
						rules    : [
								[
										action: "allow",
										os    : [
												name: getOSName(art.classifier)
										]
								]
						]
				]
			} catch (ignored) {}
		}
		return ret
	}

	// TODO: Change to standard maven
	static def getNativeURL(path, filename) {
		def urlList = [
				"https://repo.cleanroommc.com/releases/",
				"https://repo.cleanroommc.com/snapshots/", // In case we use snapshot/forked version of dependency
				"https://maven.minecraftforge.net/",
				"https://repo.maven.apache.org/maven2/",
				"https://libraries.minecraft.net/",
				"https://maven.arcseekers.com/releases/"
		]
		try {
			return urlList.stream().map(original -> original + path)
					.filter(it -> this::checkExists(it)).findFirst().get()
		} catch (NoSuchElementException ignored) {
			throw new GradleException("Can't find " + filename + " from defined repositories.\n"
					+ "Please check and make sure all repositories are setup properly.\n"
					+ "At: com.cleanroommc.gradle.helpers.tasks.Util#getNativeURL(String, String)\n"
					+ "Current defined repositories: (Sort by order)\n"
					+ String.join("\n" , urlList))
		}
	}

	static def getOSName(nativeClassifier) {
		if (nativeClassifier.contains('natives-linux')) {
			return 'linux'
		} else if (nativeClassifier.contains('natives-macos')) {
			return 'osx'
		}	else if (nativeClassifier.contains('natives-windows')) {
			return 'windows'
		}
	}

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

	static def iso8601Now() {
		new Date().iso8601()
	}

	static def sha1(file) {
		MessageDigest md = MessageDigest.getInstance('SHA-1')
		file.eachByte 4096, { bytes, size ->
			md.update(bytes, 0, size)
		}
		return md.digest().collect {String.format "%02x", it}.join()
	}

	private static def artifactTree(project, artifact) {
		if (!project.ext.has('tree_resolver'))
			project.ext.tree_resolver = 1
		def cfg = project.configurations.create('tree_resolver_' + project.ext.tree_resolver++)
		def dep = project.dependencies.create(artifact)
		cfg.dependencies.add(dep)
		def files = cfg.resolve()
		return getArtifacts(project, cfg, true)
	}
	
	private static boolean checkExists(url) {
		try {
			def code = new URL(url).openConnection().with {
				requestMethod = 'HEAD'
				connect()
				responseCode
			}
			return code == 200
		} catch (Exception e) {
			if (e.toString().contains('unable to find valid certification path to requested target'))
				throw new RuntimeException('Failed to connect to ' + url + ': Missing certificate root authority, try updating java')
			throw e
		}
	}

}