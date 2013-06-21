package es.weso.wiFetcher.utils

import java.io.FileNotFoundException

object FileUtils {
  
  def getFilePath(path : String, relativePath : Boolean) : String = {
    if(path == null) {
      throw new IllegalArgumentException("Path cannot be null")
    }
    if(relativePath) {
      val resource = getClass.getClassLoader().getResource(path)
      if(resource == null)
        throw new FileNotFoundException("Files especifies cannot be found")
      resource.getPath()
    } else
      path
  }

}