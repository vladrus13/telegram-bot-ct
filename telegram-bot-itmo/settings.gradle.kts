rootProject.name = "telegram-bot-itmo"
include("skeleton")
include("plugins:alarm")
include("plugins:shikimori")
include("plugins:shipper")
include("plugins:simple")
include("plugins:homework")
include("plugins:tempplugin")
include("plugins:practiceplugin")
findProject(":plugins:practiceplugin")?.name = "practiceplugin"
