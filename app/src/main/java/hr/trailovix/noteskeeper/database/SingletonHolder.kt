package hr.trailovix.noteskeeper.database

/*creator - constructor; T - class's type; A - constructor argument's type*/
open class SingletonHolder<out T, in A>(creator:  (A)->T) {

    private var creator: ((A)->T)? = creator
    @Volatile
    private var singleInstance: T? = null

    fun getInstance(arg: A): T{
        val i = singleInstance

        if (i!=null){
            return i
        }

        return synchronized(this){
            val i2 = singleInstance
            if (i2 != null){
                i2
            }else{
                val created = creator!!(arg)
                singleInstance = created
                creator = null
                created
            }
        }
    }
}