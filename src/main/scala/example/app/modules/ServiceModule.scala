package example.app.modules

import example.app.logging.AppLogger
import example.app.modules.{DatabaseModule, ExecutionModule}
import example.app.services.{RoleService, UserService}

class ServiceModule(database: DatabaseModule, execution: ExecutionModule) {

  lazy val roleService: RoleService = new RoleService(execution.cpuBound)

  lazy val userService: UserService = new UserService(
    database.userCollection,
    new AppLogger(classOf[UserService]),
    execution.cpuBound
  )
}
