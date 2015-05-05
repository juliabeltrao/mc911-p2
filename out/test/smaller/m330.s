%class.a = type { [1 x i8 *], i32 * }
@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp2 = call i32 (i8 *, ...)* @printf(i8 * %tmp1, i32 0)
  %tmp3 = load i32 * %tmp0
  ret i32 %tmp3
}
define i32 @__a__i(%class.a * %this) {
  %tmp4 = getelementptr %class.a * %this, i8 0, i32 1
  %tmp5 = load i32 * * %tmp4
  %tmp6 = add i32 10, 1
  %tmp8 = mul i32 4, %tmp6
  %tmp9 = call i8* @malloc ( i32 %tmp8)
  %tmp7 = bitcast i8* %tmp9 to i32*
  %tmp10 = getelementptr i32 * %tmp7, i32 0
  store i32 %tmp6, i32 * %tmp10
  store i32 * %tmp7, i32 * * %tmp4
  ret i32 0
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
