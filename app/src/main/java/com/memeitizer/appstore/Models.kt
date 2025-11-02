package com.memeitizer.appstore.model
data class Catalog(val version:Int, val title:String, val repos:List<CatalogRepo>)
data class CatalogRepo(val owner:String, val repo:String, val displayName:String, val branch:String="main", val icon:String?=null, val apps:List<CatalogApp>)
data class CatalogApp(val id:String, val name:String, val description:String?, val workflow:String, val repoConfigPath:String="repo-config.json", val gradleModule:String=":app", val variant:String="release", val inject:Map<String,String> = emptyMap())
