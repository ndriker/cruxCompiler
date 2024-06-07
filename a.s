    .comm a, 112, 8
    .globl bubblesort
bubblesort:
    enter $(8*34), $0
    movq $1, %r10
    movq %r10, -8(%rbp)
    movq -8(%rbp), %r10
    movq %r10, -16(%rbp)
L3:
    movq -16(%rbp), %r10
    movq $1, %r11
    subq %r10, %r11
    movq %r11, -16(%rbp)
    movq -16(%rbp), %r10
    cmp $1, %r10
    je L1
    movq $0, %r10
    movq %r10, -24(%rbp)
    movq -24(%rbp), %r10
    movq %r10, -16(%rbp)
    movq $0, %r10
    movq %r10, -32(%rbp)
    movq -32(%rbp), %r10
    movq %r10, -40(%rbp)
L5:
    movq $13, %r10
    movq %r10, -48(%rbp)
    movq $0, %r10
    movq $1, %rax
    movq -40(%rbp), %r11
    cmp -48(%rbp), %r11
    cmovge %rax, %r10
    movq %r10, -56(%rbp)
    movq -56(%rbp), %r10
    cmp $1, %r10
    je L2
    movq -40(%rbp), %r10
    movq %r10, -64(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -64(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -72(%rbp)
    movq -72(%rbp), %r10
    movq 0(%r10), %r11
    movq %r11, -80(%rbp)
    movq $1, %r10
    movq %r10, -88(%rbp)
    movq -40(%rbp), %r10
    addq -88(%rbp), %r10
    movq %r10, -96(%rbp)
    movq -96(%rbp), %r10
    movq %r10, -104(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -104(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -112(%rbp)
    movq -112(%rbp), %r10
    movq 0(%r10), %r11
    movq %r11, -120(%rbp)
    movq $0, %r10
    movq $1, %rax
    movq -80(%rbp), %r11
    cmp -120(%rbp), %r11
    cmovg %rax, %r10
    movq %r10, -128(%rbp)
    movq -128(%rbp), %r10
    cmp $1, %r10
    je L4
L6:
    movq $1, %r10
    movq %r10, -136(%rbp)
    movq -40(%rbp), %r10
    addq -136(%rbp), %r10
    movq %r10, -144(%rbp)
    movq -144(%rbp), %r10
    movq %r10, -40(%rbp)
    jmp L5
L4:
    movq -40(%rbp), %r10
    movq %r10, -152(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -152(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -160(%rbp)
    movq -160(%rbp), %r10
    movq 0(%r10), %r11
    movq %r11, -168(%rbp)
    movq -168(%rbp), %r10
    movq %r10, -176(%rbp)
    movq -40(%rbp), %r10
    movq %r10, -184(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -184(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -192(%rbp)
    movq $1, %r10
    movq %r10, -200(%rbp)
    movq -40(%rbp), %r10
    addq -200(%rbp), %r10
    movq %r10, -208(%rbp)
    movq -208(%rbp), %r10
    movq %r10, -216(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -216(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -224(%rbp)
    movq -224(%rbp), %r10
    movq 0(%r10), %r11
    movq %r11, -232(%rbp)
    movq -232(%rbp), %r10
    movq -192(%rbp), %r11
    movq %r10, 0(%r11)
    movq $1, %r10
    movq %r10, -240(%rbp)
    movq -40(%rbp), %r10
    addq -240(%rbp), %r10
    movq %r10, -248(%rbp)
    movq -248(%rbp), %r10
    movq %r10, -256(%rbp)
    movq a@GOTPCREL(%rip), %r11
    movq -256(%rbp), %r10
    imulq $8, %r10
    addq %r10, %r11
    movq %r11, -264(%rbp)
    movq -176(%rbp), %r10
    movq -264(%rbp), %r11
    movq %r10, 0(%r11)
    movq $1, %r10
    movq %r10, -272(%rbp)
    movq -272(%rbp), %r10
    movq %r10, -16(%rbp)
    jmp L6
L2:
    jmp L3
L1:
    leave
    ret
    .globl main
main:
