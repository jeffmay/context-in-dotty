package future.logging

object LoggerName {
  def forClass(cls: Class[_]): String = {
    val b = new StringBuilder
    val parts = cls.getPackage.getName.split('.').map(p => p.charAt(0))
    for (c <- parts) {
      b.append(c)
      b.append('.')
    }
    b.append(cls.getSimpleName)
    b.toString
  }
}
