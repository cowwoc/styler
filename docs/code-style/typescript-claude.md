# Claude TypeScript Style Guide - Detection Patterns

**File Scope**: `.ts`, `.mts`, `.tsx` files only
**Purpose**: TypeScript-specific violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### Export Organization - Scattered Throughout File
**Detection Pattern**: `^export\s+(class|interface|function|const|let|var)` before end of file
**Violation**: `export class ValidationEngine { }` (mid-file export)
**Correct**: All exports consolidated at end: `export { ValidationEngine, helper };`

### Import Organization - Incorrect Order
**Detection Pattern**: Import order validation required
**Expected Order**: 1) JSON imports, 2) External libraries, 3) Internal modules, 4) Relative imports
**Violation**: Internal import before external library import
**Correct**: Group imports by type with proper ordering

### Type Safety - Any Type Used
**Detection Pattern**: `:\s*any\b|<any>|as any`
**Violation**: `function process(data: any): any`
**Correct**: `function process(data: ProcessableData): ProcessedResult`

### Interface Naming - Generic Names
**Detection Pattern**: `interface\s+(Data|State|Props|Config)\s*{`
**Violation**: `interface Data { }`
**Correct**: `interface ParseResultData { }`

### JSDoc - Missing @throws Documentation
**Detection Pattern**: Functions with `throw` statements missing `@throws` JSDoc
**Violation**: Function throws without `@throws` documentation
**Correct**: `@throws {ValidationError} When input parameters are invalid`

### JSON Imports - Using Fetch for Static Data
**Detection Pattern**: `fetch\(['"].*\.json['"]`
**Violation**: `const config = await fetch('./config.json')`
**Correct**: `import config from './config.json' assert { type: 'json' };`

### Optional Chaining - Incorrect Usage
**Detection Pattern**: `\!\.\w+` vs `\?\.\w+` analysis required
**Violation**: Using `!` when `?` is safer, or vice versa
**Context-dependent**: Requires understanding of null safety requirements

### Enum Usage - String Values Without Explicit Assignment
**Detection Pattern**: `enum\s+\w+\s*{\s*\w+\s*,` (enum without explicit values)
**Violation**: `enum Status { Active, Inactive }`
**Correct**: `enum Status { Active = "active", Inactive = "inactive" }`

### Arrow Function Returns - Implicit Object Returns
**Detection Pattern**: `=>\s*{[^}]*}[^;]` (arrow function with object-like body)
**Violation**: `items.map(x => { value: x.amount })` (missing parentheses)
**Correct**: `items.map(x => ({ value: x.amount }))`

### Promise Error Handling - Missing Catch
**Detection Pattern**: `\.then\(.*\)(?!\s*\.catch)`
**Violation**: Promise chains without error handling
**Correct**: Always include `.catch()` for promise chains

## TIER 2 IMPORTANT - Code Review

### Interface Design - Missing Readonly Properties
**Detection Pattern**: Interface properties without `readonly` modifier where immutability expected
**Violation**: `interface TokenRange { startPos: number; endPos: number; }`
**Correct**: `interface TokenRange { readonly startPos: number; readonly endPos: number; }`

### Type Guards - Missing User-Defined Type Guards
**Detection Pattern**: `typeof` checks that could be user-defined type guards
**Violation**: `if (typeof node.position === 'number')`
**Correct**: Custom type guard function for complex type validation

### Null Assertions - Unsafe Usage
**Detection Pattern**: `\!\s*\.` or `\!\s*\[` (non-null assertions)
**Violation**: `user!.profile.name` without null safety validation
**Correct**: Proper null checking or optional chaining

### Generic Constraints - Missing Constraints
**Detection Pattern**: `<T>` without constraints where domain types expected
**Violation**: `function calculate<T>(input: T): T`
**Correct**: `function calculate<T extends CalculationInput>(input: T): T`

## TIER 3 QUALITY - Best Practices

### Union Types - Using Any Instead of Union
**Detection Pattern**: `: any` where union types would be more appropriate
**Violation**: `status: any` when status has limited valid values
**Correct**: `status: 'active' | 'inactive' | 'pending'`

### Function Overloads - Missing Overload Signatures
**Detection Pattern**: Functions with conditional return types lacking overload signatures
**Violation**: Single signature with union return type
**Correct**: Multiple overload signatures for type safety

### Destructuring - Missing for Object Parameters
**Detection Pattern**: Parameter objects accessed with dot notation
**Violation**: `function calc(options) { return options.rate * options.amount; }`
**Correct**: `function calc({ rate, amount }) { return rate * amount; }`

### Utility Types - Not Using Built-in Utility Types
**Detection Pattern**: Manual type definitions that could use utility types
**Violation**: `interface PartialUser { name?: string; age?: number; }`
**Correct**: `type PartialUser = Partial<User>`

## Detection Commands

```bash
# TIER 1 Critical Violations
grep -r "^export\s\+(class\|interface\|function\|const\)" --include="*.ts" --include="*.mts" src/  # Scattered exports
grep -r ":\s*any\b\|<any>\|as any" --include="*.ts" --include="*.mts" src/                        # Any type usage
grep -r "interface\s\+(Data\|State\|Props\|Config)\s*{" --include="*.ts" src/                     # Generic interface names
grep -r "fetch(['\"].*\.json['\"]" --include="*.ts" --include="*.mts" src/                         # JSON fetch instead of import

# TIER 2 Important Violations
grep -r "\!\s*\.\|\!\s*\[" --include="*.ts" --include="*.mts" src/                                # Non-null assertions
grep -r "<T>" --include="*.ts" --include="*.mts" src/ | grep -v "extends"                         # Missing generic constraints

# TIER 3 Quality Violations  
grep -r "options\." --include="*.ts" --include="*.mts" src/                                        # Missing destructuring
grep -r "interface.*{\s*\w+\?:" --include="*.ts" src/                                             # Manual partial types
```