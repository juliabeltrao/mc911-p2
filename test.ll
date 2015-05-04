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
define i32 @i() {
  %tmp5 = mul i32 1, 10
  %tmp6 = call i8* @malloc ( i32 %tmp5)
  %tmp4 = bitcast i8* %tmp6 to i32 **
  store [10 x i32] %tmp4, i32 * %_i
  %tmp8 = load i32 * %_i
  %tmp7 = getelementptr i32 %tmp8, i32 2
  store i32 1, i32 * %tmp7
  ret i32 0
}
%class.a = type { [1 x i8 *], [0 x i32] }
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
