; ModuleID = 'learn.c'
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-redhat-linux-gnu"

; Function Attrs: nounwind uwtable
define i32 @main() #0 {
  %1 = alloca i32, align 4
  %a = alloca i32, align 4
  %b = alloca i32, align 4
  store i32 0, i32* %1
  %2 = load i32* %a, align 4
  %3 = icmp ne i32 %2, 0
  br i1 %3, label %4, label %7

; <label>:4                                       ; preds = %0
  %5 = load i32* %b, align 4
  %6 = icmp ne i32 %5, 0
  br label %7

; <label>:7                                       ; preds = %4, %0
  %8 = phi i1 [ false, %0 ], [ %6, %4 ]
  %9 = zext i1 %8 to i32
  store i32 %9, i32* %a, align 4
  %10 = load i32* %a, align 4
  ret i32 %10
}

attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

!llvm.ident = !{!0}

!0 = metadata !{metadata !"clang version 3.5.0 (tags/RELEASE_350/final)"}
