package com.sahilmaske.peerlearn.util

object SkillAliasMapper {

    // canonical name -> list of aliases (all lowercase)
    private val aliasMap: Map<String, String> = buildMap {
        val groups = listOf(
            "C++" to listOf("cpp", "c plus plus", "c++"),
            "C" to listOf("c", "c programming", "c lang"),
            "C#" to listOf("csharp", "c sharp", "c#", "dotnet c#"),
            "JavaScript" to listOf("js", "javascript", "java script", "vanilla js"),
            "TypeScript" to listOf("ts", "typescript", "type script"),
            "Python" to listOf("py", "python", "python3", "python 3"),
            "Java" to listOf("java", "core java", "java se"),
            "Kotlin" to listOf("kotlin", "kt"),
            "Swift" to listOf("swift", "swiftui"),
            "Dart" to listOf("dart", "flutter dart"),
            "PHP" to listOf("php"),
            "Ruby" to listOf("ruby", "rb"),
            "Go" to listOf("go", "golang"),
            "Rust" to listOf("rust", "rs"),
            "R" to listOf("r", "r lang", "r programming"),
            "Scala" to listOf("scala"),
            "Perl" to listOf("perl", "pl"),
            "Objective-C" to listOf("objective c", "objc", "obj-c"),
            "SQL" to listOf("sql", "mysql", "postgresql", "postgres"),
            "HTML" to listOf("html", "html5"),
            "CSS" to listOf("css", "css3"),
            "Shell Scripting" to listOf("bash", "shell", "sh", "shell scripting"),

            // Android/mobile frameworks
            "Jetpack Compose" to listOf("compose", "jetpack compose"),
            "Android SDK" to listOf("android", "android dev", "android development"),
            "Flutter" to listOf("flutter"),
            "React Native" to listOf("react native", "rn"),

            // Web frameworks
            "React" to listOf("react", "reactjs", "react.js"),
            "Angular" to listOf("angular", "angularjs"),
            "Vue.js" to listOf("vue", "vuejs", "vue.js"),
            "Node.js" to listOf("node", "nodejs", "node.js"),
            "Express.js" to listOf("express", "expressjs", "express.js"),
            "Next.js" to listOf("next", "nextjs", "next.js"),
            "Django" to listOf("django"),
            "Flask" to listOf("flask"),
            "Spring Boot" to listOf("spring", "springboot", "spring boot"),
            "Laravel" to listOf("laravel"),

            // Databases / backend
            "Firebase" to listOf("firebase", "firestore"),
            "MongoDB" to listOf("mongo", "mongodb"),

            // Data/ML
            "Machine Learning" to listOf("ml", "machine learning"),
            "Data Science" to listOf("data science", "ds"),
            "TensorFlow" to listOf("tensorflow", "tf"),

            // Design/other
            "UI/UX Design" to listOf("ui ux", "ui/ux", "uiux", "figma design"),
            "Git" to listOf("git", "github", "version control"),
        )
        for ((canonical, aliases) in groups) {
            for (alias in aliases) put(alias, canonical)
        }
    }

    fun normalize(raw: String): String {
        val key = raw.trim().lowercase()
        return aliasMap[key] ?: raw.trim()
    }

    fun normalizeList(raw: List<String>): List<String> =
        raw.map { normalize(it) }
}