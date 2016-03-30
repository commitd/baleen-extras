package com.tenode.baleen.extras.annotators.relationships.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import com.tenode.baleen.extras.patterns.annotators.data.RelationConstraint;

public class RelationConstraintTest {

	@Test
	public void test() {
		RelationConstraint rc = new RelationConstraint("type", "subType", "pos", "source", "target");

		assertEquals("type", rc.getType());
		assertEquals("subType", rc.getSubType());
		assertEquals("pos", rc.getPos());
		assertEquals("source", rc.getSource());
		assertEquals("target", rc.getTarget());

		assertTrue(rc.isValid());

		assertTrue(rc.toString().contains("type"));
	}

	@Test
	public void testValid() {
		assertFalse(new RelationConstraint("", "subType", "pos", "source", "target").isValid());
		assertFalse(new RelationConstraint("type", "", "pos", "source", "target").isValid());
		assertFalse(new RelationConstraint("type", "subType", "", "source", "target").isValid());
		assertFalse(new RelationConstraint("type", "subType", "pos", null, "target").isValid());
		assertFalse(new RelationConstraint("type", "subType", "pos", "source", "").isValid());
		assertFalse(new RelationConstraint("type", null, "pos", "source", "").isValid());
	}

	@Test
	public void testHashCodeAndEquals() {
		assertEquals(new RelationConstraint("type", "subType", "pos", "source", "target").hashCode(),
				new RelationConstraint("type", "subType", "pos", "source", "target").hashCode());
		Assert.assertNotEquals(new RelationConstraint("type", "subType", "pos", "source", "t2").hashCode(),
				new RelationConstraint("type", "subType", "pos", "source", "target").hashCode());

		assertEquals(new RelationConstraint("type", "subType", "pos", "source", "target").hashCode(),
				new RelationConstraint("type", "subType", "pos", "source", "target").hashCode());
		Assert.assertNotEquals(new RelationConstraint("type", "subType", "pos", "source", "target"),
				new RelationConstraint("type", "subType2", "pos", "source", "target"));

	}
}
