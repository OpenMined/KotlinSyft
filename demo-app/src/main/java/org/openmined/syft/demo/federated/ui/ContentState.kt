package org.openmined.syft.demo.federated.ui

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

data class ProcessData(internal val data: List<Float>)
