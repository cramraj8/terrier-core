/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is TestCompressingMetaIndex.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.terrier.indexing.FlatJSONDocument;
import org.terrier.structures.indexing.CompressingMetaIndexBuilder;
import org.terrier.structures.indexing.MetaIndexBuilder;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

/** Unit test for CompressingMetaIndex */
public class TestCompressingMetaIndex extends ApplicationSetupBasedTest {

	static boolean validPlatform()
    {
        String osname = System.getProperty("os.name");
        if (osname.contains("Windows"))
            return false;
        return true;
    }

	@Rule
	public ExpectedException exception = ExpectedException.none();
	
	String[] docnos_in_order = new String[]{
		"doc1",
		"doc20",
		"doc3",
		"doc4"
	};
	
	@Test
	public void testNumKeysConfigurationMismatch() throws IOException
	{
		exception.expect(IllegalArgumentException.class);
		CompressingMetaIndexBuilder x = new CompressingMetaIndexBuilder(
				null, new String[]{"docno"}, new int[0], new String[0]);
		x.close();
	}

	@Test
	public void testKeysSubsetConfigurationMismatch() throws IOException
	{
		exception.expect(IllegalArgumentException.class);
		CompressingMetaIndexBuilder x = new CompressingMetaIndexBuilder(
				IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX), 
				new String[]{"docno"}, new int[]{20}, new String[]{"url"});
		x.close();
	}

	
	@Test public void testSingleKeySingleCharValue() throws Exception
	{
		testBase("meta", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
				new String[]{"a"}
			});
	}
	
	@Test public void testSingleKeyManyCharValue() throws Exception 
	{
		testBase("meta", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
				new String[]{"a"},
				new String[]{"b"},
				new String[]{"c"},
				new String[]{"d"}
			});
	}
	
	
	@Test public void testSingleKeyManyUTFCharValue() throws Exception 
	{
		testBase("meta", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
				new String[]{"\u0400"},
				new String[]{"\u0460"},
				new String[]{"\u93E0"}
			});
	}
	
	@Test public void testSingleKeyManyStringValue() throws Exception
	{
		testBase("meta", new String[]{"docno"}, new int[]{2}, new String[0], new String[][]{
				new String[]{"aa"},
				new String[]{"ba"},
				new String[]{"ca"},
				new String[]{"da"}
			});
	}
	
	
	@Test public void testSingleKeyManyUTFStringValue() throws Exception
	{
		testBase("meta", new String[]{"docno"}, new int[]{2}, new String[0], new String[][]{
				new String[]{"aa"},
				new String[]{"\u0400\u93E0"},
			});
	}
	
	@Test public void testManyKeyManyValue() throws Exception
	{
		testBase("meta", new String[]{"docno", "words"}, new int[]{1, 15}, new String[0], new String[][]{
				new String[]{"a", "The lazy cat"},
				new String[]{"b", "jumped over the"},
				new String[]{"c", "sleeping dog"},
				new String[]{"d", "today"}
			});
	}

	@Test public void testManyKeyManyValueBinaryRev() throws Exception
	{
		IndexOnDisk index = createMetaIndex("meta", new String[]{"docno", "words"}, new int[]{1, 15}, new String[0], new String[][]{
			new String[]{"a", "The lazy cat"},
			new String[]{"b", "jumped over the"},
			new String[]{"c", "sleeping dog"},
			new String[]{"d", "today"}
		});
		MetaIndex meta = index.getMetaIndex();
		for(int i=0;i<meta.size();i++)
		{
			String docno = meta.getItem("docno", i);
			assertEquals(i, meta.getDocument("docno", docno));
		}
		index.close();
		IndexUtil.deleteIndex(index.getPath(), index.getPrefix());		
	}
	
	@Test public void testDifferentName() throws Exception
	{
		testBase("differentName", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
				new String[]{"a"},
				new String[]{"b"},
				new String[]{"c"},
				new String[]{"d"}
			});
	}
		
	@Test
	public void testSingleKeyExtremeLengths() throws Exception
	{
		testBase("meta", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
			new String[]{"a"},
			new String[]{"b"},
			new String[]{"c"},
			new String[]{"d"}
		});
		
		testBase("meta", new String[]{"docno"}, new int[]{26}, new String[0], new String[][]{
				new String[]{"someweb09-ja0003-57-26118"},
		});		
	}
	
	@Test
	public void testMultipleKeyExtremeLengths() throws Exception
	{
		testBase("meta", new String[]{"docno", "other"}, new int[]{1, 5}, new String[0], new String[][]{
			new String[]{"a", "11111"},
			new String[]{"b", "11112"},
			new String[]{"c", "11113"},
			new String[]{"d", "11114"}
		});
		
		testBase("meta", new String[]{"docno"}, new int[]{26}, new String[0], new String[][]{
				new String[]{"someweb09-ja0003-57-26118"},
		});		
	}
	
	@Test
	public void testSingleKeyExceptionLength() throws Exception
	{
		exception.expect(IllegalArgumentException.class);
		testBase("meta", new String[]{"docno"}, new int[]{1}, new String[0], new String[][]{
			new String[]{"a"},
			new String[]{"bb"},
			new String[]{"c"},
			new String[]{"d"}
		});
	}
	
	@Test
	public void testMultipleKeyExceptionLength() throws Exception
	{
		exception.expect(IllegalArgumentException.class);
		testBase("meta", new String[]{"docno"}, new int[]{1,1}, new String[0], new String[][]{
			new String[]{"a", "e"},
			new String[]{"b", "ff"},
			new String[]{"c", "g"},
			new String[]{"d", "h"}
		});
	}
	
	protected IndexOnDisk createMetaIndex(String name, String[] keyNames, int[] keyLengths, String[] revKeys, String[][] data) throws Exception
	{
		IndexOnDisk index = IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		assertNotNull("Index should not be null", index);
		MetaIndexBuilder b = new CompressingMetaIndexBuilder(index, name,
				keyNames, keyLengths, revKeys);
		assertNotNull(b);
		
		for(String[] dataOne : data)
		{
			b.writeDocumentEntry(dataOne);
		}
		b.close();
		b = null;
		finishedCreatingMeta(index, name);
		return index;
	}
	
	protected void testBase(String name, String[] keyNames, int[] keyLengths, String[] revKeys, String[][] data) throws Exception
	{
		IndexOnDisk index = createMetaIndex(name, keyNames, keyLengths, revKeys, data);		
		int offset = 0;
		Set<String> rev = new HashSet<String>();
		for(String revKey : revKeys)
		{
			rev.add(revKey);
		}
		for(String key : keyNames)
		{	
			String[] meta_for_this_key = slice(data, offset);
			
			checkRandom(index, name, meta_for_this_key, key, offset, rev.contains(key));
			checkStream(index, name, meta_for_this_key, offset);					
			offset++;
		}
		index.close();
		IndexUtil.deleteIndex(index.getPath(), index.getPrefix());
	}
	
	protected static String[] slice(String[][] in, int index)
	{
		final String[] rtr = new String[in.length];
		for(int i=0;i<in.length;i++)
		{
			rtr[i] = in[i][index];
		}
		return rtr;
	}


	protected void finishedCreatingMeta(IndexOnDisk index, String name) throws Exception
	{
		assertTrue(index.hasIndexStructure(name));
		assertTrue(index.hasIndexStructureInputStream(name));
	}
//	
//	protected void checkMRInputFormat(Index index, String name, String[] docnos, long blocksize) throws Exception
//	{
//		if (! validPlatform()) return;
//		JobConf jc = HadoopPlugin.getJobFactory(this.getClass().getName()).newJob();
//		HadoopUtility.toHConfiguration(index, jc);
//		CompressingMetaIndexInputFormat.setStructure(jc, name);
//		CompressingMetaIndexInputFormat information = new CompressingMetaIndexInputFormat();
//		information.validateInput(jc);
//		information.overrideDataFileBlockSize(blocksize);
//		InputSplit[] splits = information.getSplits(jc, 2);
//		Set<String> unseenDocnos = new HashSet<String>(Arrays.asList(docnos));
//		int seenDocuments = 0;
//		for(InputSplit split : splits)
//		{
//			RecordReader<IntWritable,Wrapper<String[]>> rr = information.getRecordReader(split, jc, null);
//			IntWritable key = rr.createKey();
//			Wrapper<String[]> value = rr.createValue();
//			while(rr.next(key, value))
//			{
//				seenDocuments++;
//				String docno = value.getObject()[0];
//				unseenDocnos.remove(docno);
//				assertEquals(docnos[key.get()], docno);
//			}
//			rr.close();
//		}
//		assertEquals("Not correct number of document seen", docnos.length, seenDocuments);
//		assertEquals("Some documents unseen", 0, unseenDocnos.size());
//	}
//	
	
	@SuppressWarnings("unchecked")
	protected void checkStream(Index index, String name, String[] docnos, int ith) throws Exception
	{
		Iterator<String[]> metaIn = (Iterator<String[]>) index.getIndexStructureInputStream(name);
		assertNotNull(metaIn);
		int i = 0;
		while(metaIn.hasNext())
		{
			String[] data = metaIn.next();
			assertEquals(docnos[i], data[ith]);
			i++;
		}
		assertEquals(docnos.length, i);
		IndexUtil.close(metaIn);
	}
	
	protected void checkRandom(Index index, String name, String[] docnos, String key, int offset, boolean reverse) throws Exception
	{
		MetaIndex mi = name.equals("meta")
			? index.getMetaIndex()
			: (MetaIndex) index.getIndexStructure(name);
		assertNotNull(mi);

		if (reverse)
			assertEquals(docnos.length, ((CompressingMetaIndex)mi).forwardMetaMaps[0].size());

		
		for(int i=0;i < docnos.length; i++)
		{
			assertEquals(docnos[i], mi.getAllItems(i)[offset]);
			assertEquals(docnos[i], mi.getItem(key, i));
			assertEquals(docnos[i], mi.getItems(key, new int[]{i})[0]);
			assertEquals(docnos[i], mi.getItems(new String[]{key}, i)[0]);
			assertEquals(docnos[i], mi.getItems(new String[]{key},  new int[]{i})[0][0]);
			if (reverse)
				assertEquals(i, mi.getDocument(key, docnos[i]));
		}
		
		if (reverse)
		{
			assertEquals(-1, mi.getDocument(key, "doc"));
			assertEquals(-1, mi.getDocument(key, "doc0"));
			assertEquals(-1, mi.getDocument(key, "doc10"));
		}
		
		final int[] docids = new int[docnos.length];
		for(int i=0;i<docids.length;i++)
			docids[i] = i;
		
		final String[] retr_docnos = mi.getItems(key, docids);
		assertEquals(docids.length, retr_docnos.length);
		assertTrue(Arrays.equals(docnos, retr_docnos));
	
		final String[][] retr_docnos2 = mi.getItems(new String[]{key}, docids);
		assertEquals(docids.length, retr_docnos2.length);
		assertEquals(1, retr_docnos2[0].length);
		assertTrue(Arrays.equals(docnos, retr_docnos));
	}
	
	
	@Test
	public void testCropFunction() throws IOException {
		String separator = ApplicationSetup.FILE_SEPARATOR;
		String exampleTweetFile = ApplicationSetup.TERRIER_HOME+separator+"share"+separator+"tests"+separator+"tweets"+separator+"utf8-tweet.json";
		File tweetFile = new File(exampleTweetFile);
		assertTrue("Tweet file is available",tweetFile.exists());
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tweetFile), "UTF-8"));
		String tweet = br.readLine();
		br.close();
		
		FlatJSONDocument doc = new FlatJSONDocument(tweet);
		
		
		IndexOnDisk index = IndexOnDisk.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		
		String[] _keyNames = {"docno", "text"};
		int[] _valueLens = {20, 140};
		String[] _forwardKeys = _keyNames;
		
		String previousCropConfig = ApplicationSetup.getProperty("metaindex.compressed.crop.long", "false");
		ApplicationSetup.setProperty("metaindex.compressed.crop.long", "true");
		
		CompressingMetaIndexBuilder compressedMetaIndexBuilder;
		try {
			compressedMetaIndexBuilder = new CompressingMetaIndexBuilder(index, _keyNames, _valueLens, _forwardKeys);
			compressedMetaIndexBuilder.writeDocumentEntry(doc.getAllProperties());
		} catch (Exception e) {
			Assert.fail("Compressing MetaIndexBuilder failed to write the metadata for an example tweet. "+e.getMessage());
		}
		
		ApplicationSetup.setProperty("metaindex.compressed.crop.long", previousCropConfig);
		
		
		index.close();
		IndexUtil.deleteIndex(((IndexOnDisk)index).getPath(), ((IndexOnDisk)index).getPrefix());
		
	
	}
	
}
