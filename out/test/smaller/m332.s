%class.a = type { [2 x i8 *], label }
@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp2 = mul i32 1, 1
  %tmp3 = call i8* @malloc ( i32 %tmp2)
  %tmp1 = bitcast i8* %tmp3 to %class.a*
  %tmp4 = call i32  @__a__i(%class.a * %tmp1)
  %tmp5 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp6 = call i32 (i8 *, ...)* @printf(i8 * %tmp5, i32 %tmp4)
  %tmp7 = load i32 * %tmp0
  ret i32 %tmp7
}
define label @__a__A(%class.a * %this) {
  %tmp8 = getelementptr %class.a * %this, i8 0, i32 1
  %tmp9 = load label * %tmp8
  %tmp11 = mul i32 1, 1
  %tmp12 = call i8* @malloc ( i32 %tmp11)
  %tmp10 = bitcast i8* %tmp12 to %class.a*
  store %class.a * * %tmp10, label * %tmp8
  ret %class.a * %this
}
define i32 @__a__i(%class.a * %this) {
  ret i32 0
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
