package hr.trailovix.noteskeeper

object DummyDataHolder {
    val tasks = listOf(
        Task("avocado", isDone = true),
        Task("banana"),
        Task("cheese", isDone = true),
        Task("drinks"),
        Task("Here comes rain again", "It is raining in my town like somewhere else. My dog really doesn't like to have rain coat, but it is better for her."),
        Task("ear tips", "", isDone = true),
        Task("for lunch", "soup, bread"),
    )
}