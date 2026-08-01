package com.notekeep.local.graph

data class GraphNode(
    val id: String,
    val label: String,
    val isTag: Boolean,
    val noteId: Long? = null,
    var x: Float = 0f,
    var y: Float = 0f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var degree: Int = 0
)

data class GraphEdge(
    val sourceId: String,
    val targetId: String
)

class GraphData(
    val nodes: MutableList<GraphNode>,
    val edges: MutableList<GraphEdge>
) {
    private val indexById = nodes.associateBy { it.id }.let { HashMap(it) }

    fun nodeById(id: String): GraphNode? = indexById[id]

    companion object {
        /** Builds a bipartite graph: note nodes connected to the #tag nodes found inside them. */
        fun build(
            notes: List<com.notekeep.local.data.Note>,
            hideOrphans: Boolean
        ): GraphData {
            val nodes = LinkedHashMap<String, GraphNode>()
            val edges = mutableListOf<GraphEdge>()
            val degreeCount = HashMap<String, Int>()

            for (note in notes) {
                val tags = note.extractTags()
                if (tags.isEmpty() && hideOrphans) continue

                val noteNodeId = "note_${note.id}"
                val label = note.title.ifBlank {
                    note.content.take(18).ifBlank { "بدون عنوان" }
                }
                nodes.getOrPut(noteNodeId) {
                    GraphNode(id = noteNodeId, label = label, isTag = false, noteId = note.id)
                }

                for (tag in tags) {
                    val tagNodeId = "tag_$tag"
                    nodes.getOrPut(tagNodeId) {
                        GraphNode(id = tagNodeId, label = tag, isTag = true)
                    }
                    edges.add(GraphEdge(noteNodeId, tagNodeId))
                    degreeCount[noteNodeId] = (degreeCount[noteNodeId] ?: 0) + 1
                    degreeCount[tagNodeId] = (degreeCount[tagNodeId] ?: 0) + 1
                }
            }

            val nodeList = nodes.values.toMutableList()
            nodeList.forEach { it.degree = degreeCount[it.id] ?: 0 }

            // scatter initial positions so the simulation doesn't start from a single point
            val rnd = java.util.Random(42)
            for (node in nodeList) {
                node.x = 200f + rnd.nextFloat() * 400f
                node.y = 200f + rnd.nextFloat() * 400f
            }

            return GraphData(nodeList, edges)
        }
    }
}
