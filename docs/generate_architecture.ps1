Add-Type -AssemblyName System.Drawing

function New-Font([string]$name = 'Segoe UI', [float]$size = 12, [int]$style = 0) {
    New-Object System.Drawing.Font($name, $size, [System.Drawing.FontStyle]$style)
}

function New-Brush([int]$r, [int]$g, [int]$b) {
    New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb($r, $g, $b))
}

function New-Pen([int]$r, [int]$g, [int]$b, [float]$w = 2) {
    New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb($r, $g, $b), $w)
}

function Add-RoundedPath([float]$x, [float]$y, [float]$w, [float]$h, [float]$radius) {
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $d = 2 * $radius
    $path.AddArc($x, $y, $d, $d, 180, 90)
    $path.AddArc($x + $w - $d, $y, $d, $d, 270, 90)
    $path.AddArc($x + $w - $d, $y + $h - $d, $d, $d, 0, 90)
    $path.AddArc($x, $y + $h - $d, $d, $d, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-Box([System.Drawing.Graphics]$g, [float]$x, [float]$y, [float]$w, [float]$h,
    [System.Drawing.SolidBrush]$fill, [System.Drawing.Pen]$border, [float]$radius) {
    $path = Add-RoundedPath $x $y $w $h $radius
    $g.FillPath($fill, $path)
    if ($border) { $g.DrawPath($border, $path) }
    $path.Dispose()
}

function Draw-Center([System.Drawing.Graphics]$g, [string]$text, [System.Drawing.Font]$font,
    [System.Drawing.SolidBrush]$brush, [float]$x, [float]$y, [float]$w) {
    $size = $g.MeasureString($text, $font)
    $g.DrawString($text, $font, $brush, ($x + ($w - $size.Width) / 2), $y)
}

function Draw-Arrow([System.Drawing.Graphics]$g, [float]$x1, [float]$y1, [float]$x2, [float]$y2,
    [System.Drawing.Pen]$pen, [float]$head = 12) {
    $g.DrawLine($pen, $x1, $y1, $x2, $y2)
    $angle = [Math]::Atan2([double]($y2 - $y1), [double]($x2 - $x1))
    $a1 = [double]$angle + 0.45
    $a2 = [double]$angle - 0.45
    $px = [single]$x2
    $py = [single]$y2
    $p1x = [single]($x2 - $head * [Math]::Cos($a1))
    $p1y = [single]($y2 - $head * [Math]::Sin($a1))
    $p2x = [single]($x2 - $head * [Math]::Cos($a2))
    $p2y = [single]($y2 - $head * [Math]::Sin($a2))
    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.AddLines(@([System.Drawing.PointF]::new($px, $py), [System.Drawing.PointF]::new($p1x, $p1y), [System.Drawing.PointF]::new($p2x, $p2y)))
    $path.CloseFigure()
    $g.FillPath([System.Drawing.Brushes]::Black, $path)
    $path.Dispose()
}

$out = Join-Path $PSScriptRoot 'architecture.png'

$W = 1400
$H = 900
$bmp = New-Object System.Drawing.Bitmap($W, $H)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
$g.Clear([System.Drawing.Color]::White)

$dark   = New-Brush 34 40 52
$gray   = New-Brush 90 100 115
$fTitle = New-Font 'Segoe UI' 22 1
$fBig   = New-Font 'Segoe UI' 15 1
$fMid   = New-Font 'Segoe UI' 12 1
$fSmall = New-Font 'Segoe UI' 10.5 0

Draw-Center $g 'ARCADIA - Arquitectura del Backend' $fTitle $dark 0 24 $W

$cClientF = New-Brush 214 234 255
$cClientB = New-Pen 74 120 176 2
$cBackF   = New-Brush 241 243 246
$cBackB   = New-Pen 120 130 145 2
$cAuthF   = New-Brush 227 246 235
$cAuthB   = New-Pen 56 152 96 2
$cUserF   = New-Brush 255 244 220
$cUserB   = New-Pen 214 150 44 2
$cSecF    = New-Brush 253 232 232
$cSecB    = New-Pen 190 74 74 2
$cComF    = New-Brush 238 230 250
$cComB    = New-Pen 124 92 190 2
$cRepoF   = New-Brush 227 244 251
$cRepoB   = New-Pen 44 138 190 2
$cDbF     = New-Brush 255 224 214
$cDbB     = New-Pen 200 90 60 2

$cx = 160; $cy = 70; $cw = 1080; $ch = 80
Draw-Box $g $cx $cy $cw $ch $cClientF $cClientB 14
Draw-Center $g 'CLIENTE' $fBig $dark ($cx + 30) ($cy + 12) ($cw - 60)
Draw-Center $g 'Frontend Vue 3 + Vite (Fase 1)  |  DBeaver (conexion directa a MySQL)' $fSmall $gray ($cx + 30) ($cy + 48) ($cw - 60)

Draw-Arrow $g 700 150 700 190 (New-Pen 34 40 52 2.5)

$bx = 60; $by = 190; $bw = 1280; $bh = 270
Draw-Box $g $bx $by $bw $bh $cBackF $cBackB 14
Draw-Center $g 'SPRING BOOT - BACKEND  (localhost:8080)' $fBig $dark $bx ($by + 12) $bw

$my = $by + 52
Draw-Box $g 100 $my 380 195 $cAuthF $cAuthB 12
Draw-Center $g 'auth' $fMid $dark 100 ($my + 14) 380
Draw-Center $g 'Registro - Login - Refresh' $fSmall $gray 110 ($my + 50) 360
Draw-Center $g 'AuthController - AuthService' $fSmall $gray 110 ($my + 74) 360
Draw-Center $g 'POST /api/auth/register' $fSmall $gray 110 ($my + 108) 360
Draw-Center $g 'POST /api/auth/login' $fSmall $gray 110 ($my + 132) 360
Draw-Center $g 'POST /api/auth/refresh' $fSmall $gray 110 ($my + 156) 360

Draw-Box $g 500 $my 290 195 $cUserF $cUserB 12
Draw-Center $g 'user' $fMid $dark 500 ($my + 14) 290
Draw-Center $g 'Perfil del usuario' $fSmall $gray 510 ($my + 50) 270
Draw-Center $g 'UserController' $fSmall $gray 510 ($my + 74) 270
Draw-Center $g 'GET /api/users/me' $fSmall $gray 510 ($my + 108) 270

Draw-Box $g 810 $my 490 90 $cSecF $cSecB 12
Draw-Center $g 'security (JWT)' $fMid $dark 810 ($my + 12) 490
Draw-Center $g 'SecurityConfig - JwtService - JwtAuthenticationFilter - CustomUserDetails' $fSmall $gray 820 ($my + 50) 470

Draw-Box $g 810 ($my + 105) 490 90 $cComF $cComB 12
Draw-Center $g 'common' $fMid $dark 810 ($my + 117) 490
Draw-Center $g 'ApiResponse - GlobalExceptionHandler - excepciones' $fSmall $gray 820 ($my + 155) 470

Draw-Arrow $g 700 460 700 500 (New-Pen 34 40 52 2.5)

$ry = 500; $rh = 85
Draw-Box $g 60 $ry 1280 $rh $cRepoF $cRepoB 14
Draw-Center $g 'SPRING DATA JPA' $fMid $dark 60 ($ry + 12) 1280
Draw-Center $g 'Repositorios (UserRepository, RoleRepository, GameRepository, ...)  |  Entidades (@Entity User, Role, Game, ...)' $fSmall $gray 80 ($ry + 48) 1240

Draw-Arrow $g 700 585 700 625 (New-Pen 34 40 52 2.5)

$dy = 625; $dh = 155
Draw-Box $g 60 $dy 1280 $dh $cDbF $cDbB 14
Draw-Center $g 'MYSQL 8.4 - DOCKER' $fMid $dark 60 ($dy + 12) 1280
Draw-Center $g 'Contenedor arcadia-mysql  |  localhost:3306  |  BD arcadia' $fSmall $gray 80 ($dy + 50) 1240
Draw-Center $g 'Usuario BD: arcadia  |  esquema: db/schema.sql  |  volumen persistente: arcadia_mysql_data' $fSmall $gray 80 ($dy + 76) 1240
Draw-Center $g 'DBeaver -> JDBC: localhost:3306 - arcadia / arcadia' $fSmall $gray 80 ($dy + 112) 1240

$n1 = New-Font 'Segoe UI' 11 0
Draw-Center $g 'Swagger UI:  http://localhost:8080/swagger-ui.html' $n1 $gray 60 810 620
Draw-Center $g 'docker compose up -d   |   cd backend; ./mvnw spring-boot:run' $n1 $gray 680 810 660

$bmp.Save($out, [System.Drawing.Imaging.ImageFormat]::Png)
$g.Dispose()
$bmp.Dispose()

Write-Output "Imagen generada: $out"
