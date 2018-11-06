package karstenbot

import java.sql._

import scala.collection.mutable.ListBuffer

class Database(val s: SecretTrait) {
  def connect: Connection = {
    Class.forName("com.mysql.cj.jdbc.Driver").newInstance
    DriverManager.getConnection(url, s.dbUser, s.dbPw)
  }

  def url: String = {
    s"""jdbc:mysql://${s.dbHost}:${s.dbPort}/${s.dbName}"""
  }

  def test: Boolean = {
    var res = false
    select("show tables;", item => {
      res = true
      println(item)
    })
    res
  }

  def update(query: String, key: Int = Statement.NO_GENERATED_KEYS): Seq[Any] = {
    val stm = connect.createStatement
    val res: Int = stm.executeUpdate(query, key)
    val rs = stm.getGeneratedKeys
    var seq: ListBuffer[Any] = ListBuffer[Any]()
    if (key == Statement.RETURN_GENERATED_KEYS) {
      while (rs.next) {
        seq += rs.getObject(1)
      }
    }
    seq.toVector
  }

  def select(query: String, withItemDo: Vector[Any] => Unit): Unit = {
    val rs = connect.createStatement.executeQuery(query)

    while (rs.next) {
      val vec = 0.until(rs.getMetaData.getColumnCount).map(x => rs.getObject(x + 1)).toVector
      withItemDo(vec)
    }
  }
}