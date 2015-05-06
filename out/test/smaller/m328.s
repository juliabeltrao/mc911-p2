@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = add i32 10, 1
  %tmp3 = mul i32 4, %tmp1
  %tmp4 = call i8* @malloc ( i32 %tmp3)
  %tmp2 = bitcast i8* %tmp4 to i32*
  %tmp5 = getelementptr i32 %tmp2, i32 0
  store i32 %tmp1, i32 * %tmp5
  %tmp6 = getelementptr i32 %tmp2, i32 0
  %tmp7 = load i32 %tmp6
  %tmp8 = sub i32 %tmp7, 1
  %tmp9 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp10 = call i32 (i8 *, ...)* @printf(i8 * %tmp9, i32 %tmp8)
  %tmp11 = load i32 * %tmp0
  ret i32 %tmp11
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
