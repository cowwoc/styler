package io.github.cowwoc.styler.ast.core;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.HashMap;
import java.util.Map;

import static io.github.cowwoc.requirements12.java.DefaultJavaValidators.requireThat;

/**
 * Arena-based storage for AST nodes using index-overlay pattern.
 * Each node occupies exactly 12 bytes with the following layout:
 * <ul>
 *   <li>Bytes 0-3: NodeType ordinal (int)</li>
 *   <li>Bytes 4-7: Start position in source (int)</li>
 *   <li>Bytes 8-11: End position in source (int)</li>
 * </ul>
 */
public final class NodeArena implements AutoCloseable
{
	private static final int BYTES_PER_NODE = 12;
	private static final int INITIAL_CAPACITY = 1024;
	private static final ValueLayout.OfInt INT_LAYOUT = ValueLayout.JAVA_INT;

	// Field offsets within each 12-byte node
	private static final int TYPE_OFFSET = 0;
	private static final int START_OFFSET = 4;
	private static final int END_OFFSET = 8;

	private final Arena arena;
	private final Map<NodeIndex, NodeAttribute> attributes = new HashMap<>();
	private MemorySegment segment;
	private int nodeCount;
	private int capacity;

	/**
	 * Creates a new NodeArena with default initial capacity.
	 */
	public NodeArena()
	{
		this(INITIAL_CAPACITY);
	}

	/**
	 * Creates a new NodeArena with specified initial capacity.
	 *
	 * @param initialCapacity the initial number of nodes to allocate space for
	 * @throws IllegalArgumentException if {@code initialCapacity} is not positive
	 */
	public NodeArena(int initialCapacity)
	{
		requireThat(initialCapacity, "initialCapacity").isPositive();
		this.arena = Arena.ofConfined();
		this.capacity = initialCapacity;
		this.segment = arena.allocate(BYTES_PER_NODE * (long) capacity);
	}

	/**
	 * Allocates a new node in the arena and returns its index.
	 *
	 * @param type  the type of node to create
	 * @param start the start position in the source code
	 * @param end   the end position in the source code
	 * @return the index of the newly created node
	 * @throws NullPointerException if {@code type} is null
	 * @throws IllegalArgumentException if {@code start}/{@code end} positions are negative
	 */
	public NodeIndex allocateNode(NodeType type, int start, int end)
	{
		requireThat(type, "type").isNotNull();
		requireThat(start, "start").isNotNegative();
		requireThat(end, "end").isNotNegative();

		if (nodeCount >= capacity)
		{
			grow();
		}

		long offset = (long) nodeCount * BYTES_PER_NODE;
		segment.set(INT_LAYOUT, offset + TYPE_OFFSET, type.ordinal());
		segment.set(INT_LAYOUT, offset + START_OFFSET, start);
		segment.set(INT_LAYOUT, offset + END_OFFSET, end);

		NodeIndex result = new NodeIndex(nodeCount);
		++nodeCount;
		return result;
	}

	/**
	 * Allocates an import declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the import attribute containing the qualified name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateImportDeclaration(int start, int end, ImportAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.IMPORT_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates a module import declaration node with its associated attribute.
	 * <p>
	 * Module imports (JEP 511) import all public types exported by a module.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the module import attribute containing the module name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateModuleImportDeclaration(int start, int end, ModuleImportAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.MODULE_IMPORT_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates a package declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the package attribute containing the package name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocatePackageDeclaration(int start, int end, PackageAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.PACKAGE_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates a class declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the type declaration attribute containing the class name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateClassDeclaration(int start, int end, TypeDeclarationAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.CLASS_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates an interface declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the type declaration attribute containing the interface name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateInterfaceDeclaration(int start, int end, TypeDeclarationAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.INTERFACE_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates an enum declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the type declaration attribute containing the enum name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateEnumDeclaration(int start, int end, TypeDeclarationAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.ENUM_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates a record declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the type declaration attribute containing the record name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateRecordDeclaration(int start, int end, TypeDeclarationAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.RECORD_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates an annotation type declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the type declaration attribute containing the annotation type name
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateAnnotationTypeDeclaration(int start, int end, TypeDeclarationAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.ANNOTATION_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates a parameter declaration node with its associated attribute.
	 *
	 * @param start     the start position in the source code
	 * @param end       the end position in the source code
	 * @param attribute the parameter attribute containing the parameter name and flags
	 * @return the index of the newly created node
	 * @throws NullPointerException     if {@code attribute} is null
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateParameterDeclaration(int start, int end, ParameterAttribute attribute)
	{
		requireThat(attribute, "attribute").isNotNull();
		NodeIndex index = allocateNode(NodeType.PARAMETER_DECLARATION, start, end);
		attributes.put(index, attribute);
		return index;
	}

	/**
	 * Allocates an implicit class declaration node.
	 * <p>
	 * Implicit classes (JEP 512) contain top-level members without an explicit class declaration.
	 * The compiler generates a class name based on the source file name.
	 *
	 * @param start the start position in the source code
	 * @param end   the end position in the source code
	 * @return the index of the newly created node
	 * @throws IllegalArgumentException if {@code start} or {@code end} positions are negative
	 */
	public NodeIndex allocateImplicitClassDeclaration(int start, int end)
	{
		return allocateNode(NodeType.IMPLICIT_CLASS_DECLARATION, start, end);
	}

	/**
	 * Returns the import attribute associated with a node.
	 *
	 * @param index the node index
	 * @return the import attribute
	 * @throws NullPointerException     if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is invalid or the node is not an import declaration
	 */
	public ImportAttribute getImportAttribute(NodeIndex index)
	{
		validateIndex(index);
		if (getType(index) != NodeType.IMPORT_DECLARATION)
		{
			throw new IllegalArgumentException("Expected IMPORT_DECLARATION but was " + getType(index));
		}
		NodeAttribute attribute = attributes.get(index);
		if (attribute instanceof ImportAttribute importAttribute)
		{
			return importAttribute;
		}
		throw new AssertionError("Import node at position " + getStart(index) +
			" is missing ImportAttribute");
	}

	/**
	 * Returns the module import attribute associated with a node.
	 *
	 * @param index the node index
	 * @return the module import attribute
	 * @throws NullPointerException     if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is invalid or the node is not a module import declaration
	 */
	public ModuleImportAttribute getModuleImportAttribute(NodeIndex index)
	{
		validateIndex(index);
		if (getType(index) != NodeType.MODULE_IMPORT_DECLARATION)
		{
			throw new IllegalArgumentException("Expected MODULE_IMPORT_DECLARATION but was " + getType(index));
		}
		NodeAttribute attribute = attributes.get(index);
		if (attribute instanceof ModuleImportAttribute moduleImportAttribute)
		{
			return moduleImportAttribute;
		}
		throw new AssertionError("Module import node at position " + getStart(index) +
			" is missing ModuleImportAttribute");
	}

	/**
	 * Returns the package attribute associated with a node.
	 *
	 * @param index the node index
	 * @return the package attribute
	 * @throws NullPointerException     if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is invalid or the node is not a package declaration
	 */
	public PackageAttribute getPackageAttribute(NodeIndex index)
	{
		validateIndex(index);
		if (getType(index) != NodeType.PACKAGE_DECLARATION)
		{
			throw new IllegalArgumentException("Expected PACKAGE_DECLARATION but was " + getType(index));
		}
		NodeAttribute attribute = attributes.get(index);
		if (attribute instanceof PackageAttribute packageAttribute)
		{
			return packageAttribute;
		}
		throw new AssertionError("Package node at position " + getStart(index) +
			" is missing PackageAttribute");
	}

	/**
	 * Returns the type declaration attribute associated with a node.
	 *
	 * @param index the node index
	 * @return the type declaration attribute
	 * @throws NullPointerException     if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is invalid or the node is not a type declaration
	 */
	public TypeDeclarationAttribute getTypeDeclarationAttribute(NodeIndex index)
	{
		validateIndex(index);
		NodeType type = getType(index);
		if (!isTypeDeclaration(type))
		{
			throw new IllegalArgumentException("Expected type declaration but was " + type);
		}
		NodeAttribute attribute = attributes.get(index);
		if (attribute instanceof TypeDeclarationAttribute typeDeclarationAttribute)
		{
			return typeDeclarationAttribute;
		}
		throw new AssertionError("Type declaration node at position " + getStart(index) +
			" is missing TypeDeclarationAttribute");
	}

	/**
	 * Returns the parameter attribute associated with a node.
	 *
	 * @param index the node index
	 * @return the parameter attribute
	 * @throws NullPointerException     if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is invalid or the node is not a parameter declaration
	 */
	public ParameterAttribute getParameterAttribute(NodeIndex index)
	{
		validateIndex(index);
		if (getType(index) != NodeType.PARAMETER_DECLARATION)
		{
			throw new IllegalArgumentException("Expected PARAMETER_DECLARATION but was " + getType(index));
		}
		NodeAttribute attribute = attributes.get(index);
		if (attribute instanceof ParameterAttribute parameterAttribute)
		{
			return parameterAttribute;
		}
		throw new AssertionError("Parameter node at position " + getStart(index) +
			" is missing ParameterAttribute");
	}

	/**
	 * Checks if the given node type is a type declaration.
	 *
	 * @param type the node type to check
	 * @return {@code true} if the type is a class, interface, enum, record, annotation, or implicit class
	 *         declaration
	 */
	private static boolean isTypeDeclaration(NodeType type)
	{
		return switch (type)
		{
			case CLASS_DECLARATION, INTERFACE_DECLARATION, ENUM_DECLARATION,
				RECORD_DECLARATION, ANNOTATION_DECLARATION, IMPLICIT_CLASS_DECLARATION -> true;
			default -> false;
		};
	}

	/**
	 * Returns the memory offset for a node at the specified index.
	 * Validates the index and calculates its byte offset in the memory segment.
	 * Eliminates duplicate validation and offset calculation logic.
	 *
	 * @param index the node index
	 * @return the byte offset of the node in the memory segment
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	private long getNodeOffset(NodeIndex index)
	{
		validateIndex(index);
		return (long) index.index() * BYTES_PER_NODE;
	}

	/**
	 * Returns the type of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the node type
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public NodeType getType(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		int typeOrdinal = segment.get(INT_LAYOUT, offset + TYPE_OFFSET);
		return NodeType.values()[typeOrdinal];
	}

	/**
	 * Returns the start position of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the start position in source code
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public int getStart(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		return segment.get(INT_LAYOUT, offset + START_OFFSET);
	}

	/**
	 * Returns the end position of the node at the specified index.
	 *
	 * @param index the node index
	 * @return the end position in source code
	 * @throws IllegalArgumentException if {@code index} is invalid
	 */
	public int getEnd(NodeIndex index)
	{
		long offset = getNodeOffset(index);
		return segment.get(INT_LAYOUT, offset + END_OFFSET);
	}

	/**
	 * Returns the current number of nodes in the arena.
	 *
	 * @return the node count
	 */
	public int getNodeCount()
	{
		return nodeCount;
	}

	/**
	 * Returns the current capacity of the arena.
	 *
	 * @return the capacity in nodes
	 */
	public int getCapacity()
	{
		return capacity;
	}

	/**
	 * Calculates the memory used by this arena in bytes.
	 *
	 * @return the memory usage in bytes
	 */
	public long getMemoryUsage()
	{
		return (long) capacity * BYTES_PER_NODE;
	}

	/**
	 * Validates that the given index is within bounds.
	 *
	 * @param index the index to validate
	 * @throws NullPointerException if {@code index} is null
	 * @throws IllegalArgumentException if {@code index} is out of bounds
	 */
	private void validateIndex(NodeIndex index)
	{
		requireThat(index, "index").isNotNull();
		requireThat(index.index(), "index.index()").isLessThan(nodeCount);
	}

	/**
	 * Grows the arena capacity by doubling it.
	 *
	 * @throws IllegalStateException if growth would exceed maximum capacity
	 */
	private void grow()
	{
		int newCapacity = capacity * 2;

		// SEC-011: Arena capacity limit to prevent unbounded memory growth
		if (newCapacity > SecurityConfig.MAX_ARENA_CAPACITY)
		{
			throw new IllegalStateException(
				"Arena capacity limit exceeded: cannot grow beyond " + SecurityConfig.MAX_ARENA_CAPACITY + " nodes");
		}

		MemorySegment newSegment = arena.allocate(BYTES_PER_NODE * (long) newCapacity);

		// Copy existing data
		MemorySegment.copy(segment, 0, newSegment, 0, BYTES_PER_NODE * (long) nodeCount);

		this.segment = newSegment;
		this.capacity = newCapacity;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!(obj instanceof NodeArena other))
			return false;
		if (nodeCount != other.nodeCount)
			return false;

		// Compare all nodes by type, start, and end positions
		for (int i = 0; i < nodeCount; ++i)
		{
			NodeIndex index = new NodeIndex(i);
			if (getType(index) != other.getType(index))
				return false;
			if (getStart(index) != other.getStart(index))
				return false;
			if (getEnd(index) != other.getEnd(index))
				return false;
		}

		// Compare attributes
		return attributes.equals(other.attributes);
	}

	@Override
	public int hashCode()
	{
		int result = nodeCount;
		for (int i = 0; i < nodeCount; ++i)
		{
			NodeIndex index = new NodeIndex(i);
			result = 31 * result + getType(index).hashCode();
			result = 31 * result + getStart(index);
			result = 31 * result + getEnd(index);
		}
		result = 31 * result + attributes.hashCode();
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(256);
		sb.append("NodeArena[nodeCount=").append(nodeCount).append(", nodes=[");
		for (int i = 0; i < nodeCount; ++i)
		{
			if (i > 0)
				sb.append(", ");
			NodeIndex index = new NodeIndex(i);
			sb.append(getType(index)).append('(').
				append(getStart(index)).append(", ").
				append(getEnd(index)).append(')');
			NodeAttribute attr = attributes.get(index);
			if (attr != null)
				sb.append(" attr=").append(attr);
		}
		sb.append("]]");
		return sb.toString();
	}

	@Override
	public void close()
	{
		arena.close();
	}
}
