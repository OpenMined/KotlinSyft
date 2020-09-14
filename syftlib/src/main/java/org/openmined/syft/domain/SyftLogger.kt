package org.openmined.syft.domain

interface SyftLogger {
    fun postData(result: Float) {
        print(result)
    }

    fun postEpoch(epoch: Int) {
        print(epoch)
    }

    fun postLog(message: String) {
        print(message)
    }

    fun postState(status: ContentState) {
        print(status)
    }
}

sealed class ContentState {
    companion object {
        fun getObjectFromString(type: String?): ContentState? {
            return when (type) {
                "training" -> Training
                "loading" -> Loading
                else -> null
            }
        }
    }

    object Training : ContentState() {
        override fun toString(): String {
            return "training"
        }
    }

    object Loading : ContentState() {
        override fun toString(): String {
            return "loading"
        }
    }
}

data class ProcessData(val data: List<Float>)