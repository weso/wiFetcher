package es.weso.wiFetcher

import es.wes.wiFetcher.fetchers.IndabaFetcher

object Main {

  def main(args: Array[String]): Unit = {
    val f = new IndabaFetcher
    f.fetch("demo.csv")
  }

}