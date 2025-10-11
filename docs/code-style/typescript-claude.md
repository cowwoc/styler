# Claude TypeScript Style Guide - Detection Patterns

**File Scope**: `.ts`, `.mts`, `.tsx` files only
**Purpose**: TypeScript-specific violation detection optimized for Claude

## TIER 1 CRITICAL - Build Blockers

### Export Organization - Scattered Throughout File
**Pattern**: `^export\s+(class|interface|function|const|let|var)` before end | **Violation**: Mid-file exports | **Correct**: Consolidate at end: `export { ValidationEngine, helper };`

### Import Organization - Incorrect Order
**Pattern**: Import order validation | **Expected Order**: JSON imports → External libraries → Internal modules → Relative imports | **Violation**: Wrong order | **Correct**: Group by type with proper ordering

### Type Safety - Any Type Used
**Pattern**: `:\s*any\b|<any>|as any` | **Violation**: `function process(data: any): any` | **Correct**: `function process(data: ProcessableData): ProcessedResult`

### Interface Naming - Generic Names
**Pattern**: `interface\s+(Data|State|Props|Config)\s*{` | **Violation**: `interface Data { }` | **Correct**: `interface ParseResultData { }`

### JSDoc - Missing @throws Documentation
**Pattern**: Functions with `throw` missing `@throws` JSDoc | **Violation**: Throws without docs | **Correct**: `@throws {ValidationError} When input parameters are invalid`

### JSON Imports - Using Fetch for Static Data
**Pattern**: `fetch\(['"].*\.json['"]` | **Violation**: `const config = await fetch('./config.json')` | **Correct**: `import config from './config.json' assert { type: 'json' };`

### Optional Chaining - Incorrect Usage
**Pattern**: `\!\.\w+` vs `\?\.\w+` analysis required | **Violation**: Using `!` when `?` safer, or vice versa | **Context-dependent**: Requires null safety understanding

### Enum Usage - String Values Without Explicit Assignment
**Pattern**: `enum\s+\w+\s*{\s*\w+\s*,` | **Violation**: `enum Status { Active, Inactive }` | **Correct**: `enum Status { Active = "active", Inactive = "inactive" }`

### Arrow Function Returns - Implicit Object Returns
**Pattern**: `=>\s*{[^}]*}[^;]` | **Violation**: `items.map(x => { value: x.amount })` (missing parens) | **Correct**: `items.map(x => ({ value: x.amount }))`

### Promise Error Handling - Missing Catch
**Pattern**: `\.then\(.*\)(?!\s*\.catch)` | **Violation**: Promise chains without error handling | **Correct**: Always include `.catch()`

## TIER 2 IMPORTANT - Code Review

### Interface Design - Missing Readonly Properties
**Pattern**: Interface properties without `readonly` where immutability expected | **Violation**: `interface TokenRange { startPos: number; }` | **Correct**: `interface TokenRange { readonly startPos: number; }`

### Type Guards - Missing User-Defined Type Guards
**Pattern**: `typeof` checks that could be type guards | **Violation**: `if (typeof node.position === 'number')` | **Correct**: Custom type guard function for complex validation

### Null Assertions - Unsafe Usage
**Pattern**: `\!\s*\.` or `\!\s*\[` | **Violation**: `user!.profile.name` without validation | **Correct**: Proper null checking or optional chaining

### Generic Constraints - Missing Constraints
**Pattern**: `<T>` without constraints where domain types expected | **Violation**: `function calculate<T>(input: T): T` | **Correct**: `function calculate<T extends CalculationInput>(input: T): T`

## TIER 3 QUALITY - Best Practices

### Union Types - Using Any Instead of Union
**Pattern**: `: any` where union types more appropriate | **Violation**: `status: any` when limited valid values | **Correct**: `status: 'active' | 'inactive' | 'pending'`

### Function Overloads - Missing Overload Signatures
**Pattern**: Functions with conditional return types lacking overload signatures | **Violation**: Single signature with union return | **Correct**: Multiple overload signatures for type safety

### Destructuring - Missing for Object Parameters
**Pattern**: Parameter objects accessed with dot notation | **Violation**: `function calc(options) { return options.rate * options.amount; }` | **Correct**: `function calc({ rate, amount }) { return rate * amount; }`

### Utility Types - Not Using Built-in Utility Types
**Pattern**: Manual type definitions that could use utility types | **Violation**: `interface PartialUser { name?: string; age?: number; }` | **Correct**: `type PartialUser = Partial<User>`

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