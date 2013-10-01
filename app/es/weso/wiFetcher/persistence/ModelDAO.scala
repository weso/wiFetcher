package es.weso.wiFetcher.persistence

trait ModelDAO[T]{
  
  def store(model: T)

}