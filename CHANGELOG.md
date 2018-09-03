v4.0.0
======

**Breaking Changes**

* `ObjectFilter` is replaced by `Filter`
* `NitriteCollection` and related classes have been moved under package `org.dizitart.no2.collection`
* `ObjectRepository` and related classes have been moved under package `org.dizitart.no2.collection.objects`
* Further refactoring of `NitriteMapper` interface
* Several other classes have been moved and refactored
* Indexing logic has been refactored for better extension 

**New Changes**

* Spatial indexing support added #34
* RxJava support #107

v3.2.0
======

**New Changes**

* Closeable `NitriteCollection` and `ObjectRepository` #108

**Fixes**


v3.1.0
======

**New Changes**

* Keyed `ObjectRepository` support #78
* Podam version upgraded to resolve missing JAX-WS dependency in Java 9 #90
* MVStore upgraded to latest release #69
* Introduced a utility method to register jackson modules in `NitriteBuilder` #94
* Null order support during sort #98
* `@InheritIndices` now works for fields with any modifier #101


**Fixes**

* Fixed documentation for `MapperFacade` #100
* Added documentation for `@NitriteId` annotation #102
* Changes to text index not saved correctly #105
* Closing the database recreates dropped collections #106

v3.0.2
======

**Fixes**

* Recover should return success/failure #89
* Reopening issue #72, with variation of failing scenario still broken in 3.0.1 #93


v3.0.1
======

**New Changes**

* Jackson modules are auto discoverable #68
* Refactoring of NitriteMapper #74
* Make runtime shutdown hook optional #84


**Fixes**

* Fix for order by using a nullable columns #72
* Fix for DataGate server for Windows #71
* Intermittent NPE in remove #76
* Fix for NPE in indexing #77
* Documentation for POJO annotation #81


v3.0.0
======

**New Changes**

* KNO2JacksonMapper is now extendable
* Support for NitriteId as id field of an object
* Object's property can be updated with `null`
* Support for java.time & it's backport
* Change in `update` operation behavior (breaking changes)

**Fixes**

* ConcurrentModificationException in `NitriteEventBus` - #52
* Duplicate `@Id` in concurrent modification - #55
* Fixed a race condition while updating the index entries - #58
* Fix for sort operation - #62
* Version upgraded for several dependencies - #64

v2.1.1
======

**New Changes**

* Kotlin version upgrade to 1.2.20
* Data import export extension added in potassium-nitrite


**Fixes**

* Fixes concurrency problem while compacting database - #41
* Lucene example fixed for update and lucene version upgraded - #44 
* Fixed collection registry and repository registry - #42 
* Readme updated with potassium-nitrite - #49 


v2.1.0
======

**New Changes**

* Introduced potassium-nitrite - kotlin extension library for nitrite
* Multi-language text tokenizer support - #36 
* Cursor join - #33 
* Inherit `@Id`, `@Index` annotations from super class - #37 
* Default executor behaves like CachedThreadPool executor - #32 

**Fixes**

* Put a check on object if it is serializable - #31


v2.0.1
======

**Issue Fixes**

*  Fix for SOE - #29
*  Fix for sync issue - #25 
*  Detailed log added in JacksonMapper

v2.0.0
======

**New Changes**

* Introduced ```Mappable``` interface to speed up pojo to document conversion in Android - #18 

**Breaking Changes**

* ```NitriteMapper``` and ```JacksonMapper``` moved from package ```org.dizitart.no2.internals``` to ```org.dizitart.no2.common.mapper```

**Issue Fixes**

*  Fix for ```ObjectFilters.ALL``` - #14
*  Fix for ```dropIndex()``` - #22 
*  Documentation added - #12, #20 

v1.0.1
======

* Minor bug fixes for DataGate server - #6 , #7 , #8 
* File parameter added while opening a database - #5 
* Documentation updated - #3 , #8 


v1.0
======

* Initial release