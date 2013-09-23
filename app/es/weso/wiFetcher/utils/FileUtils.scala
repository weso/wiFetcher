package es.weso.wiFetcher.utils

import java.io.FileNotFoundException
import play.api.Play

case class FileUtils()

object FileUtils {
  
  /**
   * This method has to obtain an absolute path given a path and a boolean. 
   * This path can be absolute or relative. If path is relative, we obtain the 
   * resource and it's absolute path. Otherwise we returns the given path
   * @param path The given path
   * @param relativePath A boolean that indicates if the given path is relative
   * or absolute
   * @return An absolute path
   */
  def getFilePath(path : String, relativePath : Boolean) : String = {
    if(path == null) {
      throw new IllegalArgumentException("Path cannot be null")
    }
    if(relativePath) {
      val resource = classOf[FileUtils].getClassLoader().getResource(path)
      if(resource == null)
        throw new FileNotFoundException("File especifies does not exist")
      resource.getPath()
    } else
      path
  }

}