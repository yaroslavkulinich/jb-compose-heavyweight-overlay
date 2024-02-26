import gov.nasa.worldwind.layers.mercator.BasicMercatorTiledImageLayer
import gov.nasa.worldwind.layers.mercator.MercatorTileUrlBuilder
import java.net.URL

class ArcGisWorldImageryLayer() : BasicMercatorTiledImageLayer(
    "ArcGisWorldImagery",
    "Earth/World_Imagery",
    18,
    256,
    false,
    ".dds",
    URLBuilder(ArcGisUrlBuilder())
) {
    class URLBuilder(private val urlBuilder: ArcGisUrlBuilder) : MercatorTileUrlBuilder() {
        override fun getMercatorURL(x: Int, y: Int, z: Int): URL {
            return URL(urlBuilder.build(x, y, z))
        }
    }
}

class ArcGisUrlBuilder {
    fun build(x: Int, y: Int, z: Int): String {
        return "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/$z/$y/$x"
    }
}
