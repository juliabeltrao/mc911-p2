%class.a = type { [1 x i8 *], i32 * }
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
define i32 @__a__i(%class.a * %this) {
  %tmp8 = getelementptr %class.a * %this, i8 0, i32 1
  %tmp9 = load i32 * * %tmp8
  %tmp10 = add i32 10, 1
  %tmp12 = mul i32 4, %tmp10
  %tmp13 = call i8* @malloc ( i32 %tmp12)
  %tmp11 = bitcast i8* %tmp13 to i32*
  %tmp14 = getelementptr i32 * %tmp11, i32 0
  store i32 10, i32 * %tmp14
  store i32 * %tmp11, i32 * * %tmp8
  %tmp16 = getelementptr %class.a * %this, i8 0, i32 1
  %tmp17 = load i32 * * %tmp16
  %tmp18 = add i32 2, 1
  %tmp15 = getelementptr i32 * %tmp17, i32 %tmp18
  store i32 1, i32 * %tmp15
  %tmp19 = getelementptr %class.a * %this, i8 0, i32 1
  %tmp20 = load i32 * * %tmp19
  %tmp21 = getelementptr i32 * %tmp20, i32 0
  %tmp22 = load i32 * %tmp21
  ret i32 %tmp22
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
