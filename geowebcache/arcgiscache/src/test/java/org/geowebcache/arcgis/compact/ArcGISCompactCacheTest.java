package org.geowebcache.arcgis.compact;

import junit.framework.TestCase;
import org.geowebcache.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Unit test for ArcGIS compact cache classes. Available data in supplied test cache:
 * 
 * zoom level | min row | max row | min col | max col
 *            |         |         |         |
 *      5     |   10    |    13   |    4    |    10
 *            |         |         |         | 
 *      6     |   22    |    28   |   10    |    21
 * 
 * - image format is JPEG
 * - tile size for (5,12,7) is 6342 bytes 
 * - tile size for (6,25,17) is 6308 bytes
 * 
 * Not verifiable with this unit test because the supplied test cache is too small:
 * 
 * - zoom levels can contain more than one .bundle/.bundlx file 
 * - row and column numbers have at least 4 digits in bundle
 *   file name, but with really big caches row and column numbers
 *   can have more than 4 digits
 * 
 * 
 * @author Bjoern Saxe
 * 
 */
public class ArcGISCompactCacheTest extends TestCase {
    private final static byte[] JFIFHeader = { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
        0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01 };

    public void testCompactCacheV1() throws Exception {
        URL url = getClass().getResource("/compactcache/_alllayers/");
        ArcGISCompactCache cache = new ArcGISCompactCacheV1(url.toURI().getPath());

        assertNotNull(cache);

        assertNull(cache.getBundleFileResource(5, -1, -1));
        assertNull(cache.getBundleFileResource(4, 10, 4));
        assertNull(cache.getBundleFileResource(7, 22, 10));

        assertNull(cache.getBundleFileResource(5, 0, 0));
        assertNotNull(cache.getBundleFileResource(5, 10, 4));
        assertNotNull(cache.getBundleFileResource(5, 13, 10));
        assertNotNull(cache.getBundleFileResource(5, 12, 7));

        assertNull(cache.getBundleFileResource(6, 0, 0));
        assertNotNull(cache.getBundleFileResource(6, 22, 10));
        assertNotNull(cache.getBundleFileResource(6, 22, 10));
        assertNotNull(cache.getBundleFileResource(6, 25, 17));
    }

    public void testCompactCacheV2() throws Exception {
        /*
        URL url = getClass().getResource("/compactcacheV2/_alllayers/");
        ArcGISCompactCache cache = new ArcGISCompactCacheV2(url.toURI().getPath());

        assertNotNull(cache);

        assertNull(cache.getBundleFileResource(5, -1, -1));
        assertNull(cache.getBundleFileResource(4, 10, 4));
        assertNull(cache.getBundleFileResource(7, 22, 10));

        assertNull(cache.getBundleFileResource(5, 0, 0));
        assertNotNull(cache.getBundleFileResource(5, 10, 4));
        assertNotNull(cache.getBundleFileResource(5, 13, 10));
        assertNotNull(cache.getBundleFileResource(5, 12, 7));

        assertNull(cache.getBundleFileResource(6, 0, 0));
        assertNotNull(cache.getBundleFileResource(6, 22, 10));
        assertNotNull(cache.getBundleFileResource(6, 22, 10));
        assertNotNull(cache.getBundleFileResource(6, 25, 17));
        */
    }

    public void testBundleFileResource() throws Exception {
        URL url = getClass().getResource("/compactcache/_alllayers/");
        ArcGISCompactCache cache = new ArcGISCompactCacheV1(url.toURI().getPath());

        assertNotNull(cache);

        Resource resource = cache.getBundleFileResource(5, 0, 0);
        assertNull(resource);

        resource = cache.getBundleFileResource(5, 12, 7);
        assertNotNull(resource);
        assertEquals(6342, resource.getSize());

        File f = new File("5_12_7.jpg");
        FileOutputStream fos = new FileOutputStream(f);
        resource.transferTo(fos.getChannel());
        fos.close();

        assertTrue(startsWithJPEGHeader(f));

        f.delete();

        resource = cache.getBundleFileResource(6, 25, 17);
        assertNotNull(resource);
        assertEquals(6308, resource.getSize());

        f = new File("6_25_17.jpg");
        fos = new FileOutputStream(f);
        resource.transferTo(fos.getChannel());
        fos.close();

        assertTrue(startsWithJPEGHeader(f));

        f.delete();
    }

    private boolean startsWithJPEGHeader(File f) {
        try {
            FileInputStream fis = new FileInputStream(f);

            byte[] fileHeader = new byte[JFIFHeader.length];

            fis.read(fileHeader, 0, JFIFHeader.length);
            fis.close();

            for (int i = 0; i < fileHeader.length; i++) {
                if (fileHeader[i] != JFIFHeader[i])
                    return false;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
