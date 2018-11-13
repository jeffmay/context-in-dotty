package example.app.modules

import example.database.collections.UserCollection

class DatabaseModule {

  lazy val userCollection: UserCollection = UserCollection.example
}
