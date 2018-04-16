package nl.liacs.subdisc;

import java.lang.*;

/**
 * The gamma, digamma, and incomplete gamma functions.
 * 
 * @author Haifeng Li
 */
public class Gamma {

    /** Utility classes should not have public constructors. */
    private Gamma() {

    }

    /**
     *  A small number close to the smallest representable floating point number.
     */
    private static final double FPMIN = 1e-300;
    /**
     * Lanczos Gamma Function approximation - N (number of coefficients - 1)
     */
    private static final int LANCZOS_N = 6;
    /**
     * Lanczos Gamma Function approximation - Coefficients
     */
    private static final double[] LANCZOS_COEFF = {1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
    /**
     * Lanczos Gamma Function approximation - small gamma
     */
    private static final double LANCZOS_SMALL_GAMMA = 5.0;
    /**
     * Maximum number of iterations allowed in Incomplete Gamma Function calculations
     */
    private static final int INCOMPLETE_GAMMA_MAX_ITERATIONS = 1000;
    /**
     * Tolerance used in terminating series in Incomplete Gamma Function calculations
     */
    private static final double INCOMPLETE_GAMMA_EPSILON = 1.0E-8;

    /**
     * Gamma function. Lanczos approximation (6 terms).
     */
    public static double gamma(double x) {
        double xcopy = x;
        double first = x + LANCZOS_SMALL_GAMMA + 0.5;
        double second = LANCZOS_COEFF[0];
        double fg = 0.0;

        if (x >= 0.0) {
            if (x >= 1.0 && x - (int) x == 0.0) {
                fg = factorial((int) x - 1);
            } else {
                first = Math.pow(first, x + 0.5) * Math.exp(-first);
                for (int i = 1; i <= LANCZOS_N; i++) {
                    second += LANCZOS_COEFF[i] / ++xcopy;
                }
                fg = first * Math.sqrt(2.0 * Math.PI) * second / x;
            }
        } else {
            fg = -Math.PI / (x * gamma(-x) * Math.sin(Math.PI * x));
        }
        return fg;
    }

    /**
     * log of the Gamma function. Lanczos approximation (6 terms)
     */
    public static double lgamma(double x) {
        double xcopy = x;
        double fg = 0.0;
        double first = x + LANCZOS_SMALL_GAMMA + 0.5;
        double second = LANCZOS_COEFF[0];

        if (x >= 0.0) {
            if (x >= 1.0 && x - (int) x == 0.0) {
                fg = logFactorial((int) x - 1);
            } else {
                first -= (x + 0.5) * Math.log(first);
                for (int i = 1; i <= LANCZOS_N; i++) {
                    second += LANCZOS_COEFF[i] / ++xcopy;
                }
                fg = Math.log(Math.sqrt(2.0 * Math.PI) * second / x) - first;
            }
        } else {
            fg = Math.PI / (gamma(1.0 - x) * Math.sin(Math.PI * x));

            if (fg != 1.0 / 0.0 && fg != -1.0 / 0.0) {
                if (fg < 0) {
                    throw new IllegalArgumentException("The gamma function is negative");
                } else {
                    fg = Math.log(fg);
                }
            }
        }
        return fg;
    }

    /**
     * Regularized Incomplete Gamma Function
     * P(s,x) = <i><big>&#8747;</big><sub><small>0</small></sub><sup><small>x</small></sup> e<sup>-t</sup> t<sup>(s-1)</sup> dt</i>
     */
    public static double regularizedIncompleteGamma(double s, double x) {
        if (s < 0.0) {
            throw new IllegalArgumentException("Invalid s: " + s);
        }

        if (x < 0.0) {
            throw new IllegalArgumentException("Invalid x: " + x);
        }

        double igf = 0.0;

        if (x < s + 1.0) {
            // Series representation
            igf = regularizedIncompleteGammaSeries(s, x);
        } else {
            // Continued fraction representation
            igf = regularizedIncompleteGammaFraction(s, x);
        }

        return igf;
    }

    /**
     * Regularized Upper/Complementary Incomplete Gamma Function
     * Q(s,x) = 1 - P(s,x) = 1 - <i><big>&#8747;</big><sub><small>0</small></sub><sup><small>x</small></sup> e<sup>-t</sup> t<sup>(s-1)</sup> dt</i>
     */
    public static double regularizedUpperIncompleteGamma(double s, double x) {
        if (s < 0.0) {
            throw new IllegalArgumentException("Invalid s: " + s);
        }

        if (x < 0.0) {
            throw new IllegalArgumentException("Invalid x: " + x);
        }

        double igf = 0.0;

        if (x != 0.0) {
            if (x == 1.0 / 0.0) {
                igf = 1.0;
            } else {
                if (x < s + 1.0) {
                    // Series representation
                    igf = 1.0 - regularizedIncompleteGammaSeries(s, x);
                } else {
                    // Continued fraction representation
                    igf = 1.0 - regularizedIncompleteGammaFraction(s, x);
                }
            }
        }
        return igf;
    }

    /**
     * Regularized Incomplete Gamma Function P(a,x) = <i><big>&#8747;</big><sub><small>0</small></sub><sup><small>x</small></sup> e<sup>-t</sup> t<sup>(a-1)</sup> dt</i>.
     * Series representation of the function - valid for x < a + 1
     */
    private static double regularizedIncompleteGammaSeries(double a, double x) {
        if (a < 0.0 || x < 0.0 || x >= a + 1) {
            throw new IllegalArgumentException(String.format("Invalid a = %f, x = %f", a, x));
        }

        int i = 0;
        double igf = 0.0;
        boolean check = true;

        double acopy = a;
        double sum = 1.0 / a;
        double incr = sum;
        double loggamma = lgamma(a);

        while (check) {
            ++i;
            ++a;
            incr *= x / a;
            sum += incr;
            if (Math.abs(incr) < Math.abs(sum) * INCOMPLETE_GAMMA_EPSILON) {
                igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
                check = false;
            }
            if (i >= INCOMPLETE_GAMMA_MAX_ITERATIONS) {
                check = false;
                igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
                //logger.error("Gamma.regularizedIncompleteGammaSeries: Maximum number of iterations wes exceeded");
            }
        }
        return igf;
    }

    /**
     * Regularized Incomplete Gamma Function P(a,x) = <i><big>&#8747;</big><sub><small>0</small></sub><sup><small>x</small></sup> e<sup>-t</sup> t<sup>(a-1)</sup> dt</i>.
     * Continued Fraction representation of the function - valid for x &ge; a + 1
     * This method follows the general procedure used in Numerical Recipes.
     */
    private static double regularizedIncompleteGammaFraction(double a, double x) {
        if (a < 0.0 || x < 0.0 || x < a + 1) {
            throw new IllegalArgumentException(String.format("Invalid a = %f, x = %f", a, x));
        }

        int i = 0;
        double ii = 0.0;
        double igf = 0.0;
        boolean check = true;

        double loggamma = lgamma(a);
        double numer = 0.0;
        double incr = 0.0;
        double denom = x - a + 1.0;
        double first = 1.0 / denom;
        double term = 1.0 / FPMIN;
        double prod = first;

        while (check) {
            ++i;
            ii = (double) i;
            numer = -ii * (ii - a);
            denom += 2.0D;
            first = numer * first + denom;
            if (Math.abs(first) < FPMIN) {
                first = FPMIN;
            }
            term = denom + numer / term;
            if (Math.abs(term) < FPMIN) {
                term = FPMIN;
            }
            first = 1.0D / first;
            incr = first * term;
            prod *= incr;
            if (Math.abs(incr - 1.0D) < INCOMPLETE_GAMMA_EPSILON) {
                check = false;
            }
            if (i >= INCOMPLETE_GAMMA_MAX_ITERATIONS) {
                check = false;
                //logger.error("Gamma.regularizedIncompleteGammaFraction: Maximum number of iterations wes exceeded");
            }
        }
        igf = 1.0 - Math.exp(-x + a * Math.log(x) - loggamma) * prod;
        return igf;
    }

    /**
     * The digamma function is defined as the logarithmic derivative of the gamma function.
     */
    public static double digamma(double x) {
        final double C7[][] = {
            {
                1.3524999667726346383e4, 4.5285601699547289655e4,
                4.5135168469736662555e4, 1.8529011818582610168e4,
                3.3291525149406935532e3, 2.4068032474357201831e2,
                5.1577892000139084710, 6.2283506918984745826e-3
            },
            {
                6.9389111753763444376e-7, 1.9768574263046736421e4,
                4.1255160835353832333e4, 2.9390287119932681918e4,
                9.0819666074855170271e3, 1.2447477785670856039e3,
                6.7429129516378593773e1, 1.0
            }
        };

        final double C4[][] = {
            {
                -2.728175751315296783e-15, -6.481571237661965099e-1,
                -4.486165439180193579, -7.016772277667586642,
                -2.129404451310105168
            },
            {
                7.777885485229616042, 5.461177381032150702e1,
                8.929207004818613702e1, 3.227034937911433614e1,
                1.0
            }
        };

        double prodPj = 0.0;
        double prodQj = 0.0;
        double digX = 0.0;

        if (x >= 3.0) {
            double x2 = 1.0 / (x * x);
            for (int j = 4; j >= 0; j--) {
                prodPj = prodPj * x2 + C4[0][j];
                prodQj = prodQj * x2 + C4[1][j];
            }
            digX = Math.log(x) - (0.5 / x) + (prodPj / prodQj);

        } else if (x >= 0.5) {
            final double X0 = 1.46163214496836234126;
            for (int j = 7; j >= 0; j--) {
                prodPj = x * prodPj + C7[0][j];
                prodQj = x * prodQj + C7[1][j];
            }
            digX = (x - X0) * (prodPj / prodQj);

        } else {
            double f = (1.0 - x) - Math.floor(1.0 - x);
            digX = digamma(1.0 - x) + Math.PI / Math.tan(Math.PI * f);
        }

        return digX;
    }

    /**
     * The inverse of regularized incomplete gamma function.
     */
    public static double inverseRegularizedIncompleteGamma(double a, double p) {
        if (a <= 0.0) {
            throw new IllegalArgumentException("a must be pos in invgammap");
        }

        final double EPS = 1.0E-8;

        double x, err, t, u, pp;
        double lna1 = 0.0;
        double afac = 0.0;
        double a1 = a - 1;
        double gln = lgamma(a);
        if (p >= 1.) {
            return Math.max(100., a + 100. * Math.sqrt(a));
        }

        if (p <= 0.0) {
            return 0.0;
        }

        if (a > 1.0) {
            lna1 = Math.log(a1);
            afac = Math.exp(a1 * (lna1 - 1.) - gln);
            pp = (p < 0.5) ? p : 1. - p;
            t = Math.sqrt(-2. * Math.log(pp));
            x = (2.30753 + t * 0.27061) / (1. + t * (0.99229 + t * 0.04481)) - t;
            if (p < 0.5) {
                x = -x;
            }
            x = Math.max(1.e-3, a * Math.pow(1. - 1. / (9. * a) - x / (3. * Math.sqrt(a)), 3));
        } else {
            t = 1.0 - a * (0.253 + a * 0.12);
            if (p < t) {
                x = Math.pow(p / t, 1. / a);
            } else {
                x = 1. - Math.log(1. - (p - t) / (1. - t));
            }
        }
        for (int j = 0; j < 12; j++) {
            if (x <= 0.0) {
                return 0.0;
            }
            err = regularizedIncompleteGamma(a, x) - p;
            if (a > 1.) {
                t = afac * Math.exp(-(x - a1) + a1 * (Math.log(x) - lna1));
            } else {
                t = Math.exp(-x + a1 * Math.log(x) - gln);
            }
            u = err / t;
            x -= (t = u / (1. - 0.5 * Math.min(1., u * ((a - 1.) / x - 1))));
            if (x <= 0.) {
                x = 0.5 * (x + t);
            }
            if (Math.abs(t) < EPS * x) {
                break;
            }
        }
        return x;
    }
    
    public static long factorial(int number) {
        long result = 1;

        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }

        return result;
    }
    
    public static double logFactorial(int n)
    {

        if (n < 0)
        {
            String msg = String.format("Invalid input argument {0}. Argument must be nonnegative.", n);
            //throw new IllegalArgumentException(msg);
            return 0;
        }
        else if (n > 254)
        {
            double x = n + 1;
            return (x - 0.5)*Math.log(x) - x + 0.5*Math.log(2*Math.PI) + 1.0/(12.0*x);
        }
        else
        {
            double[] lf =
            {
                0.000000000000000,
                0.000000000000000,
                0.693147180559945,
                1.791759469228055,
                3.178053830347946,
                4.787491742782046,
                6.579251212010101,
                8.525161361065415,
                10.604602902745251,
                12.801827480081469,
                15.104412573075516,
                17.502307845873887,
                19.987214495661885,
                22.552163853123421,
                25.191221182738683,
                27.899271383840894,
                30.671860106080675,
                33.505073450136891,
                36.395445208033053,
                39.339884187199495,
                42.335616460753485,
                45.380138898476908,
                48.471181351835227,
                51.606675567764377,
                54.784729398112319,
                58.003605222980518,
                61.261701761002001,
                64.557538627006323,
                67.889743137181526,
                71.257038967168000,
                74.658236348830158,
                78.092223553315307,
                81.557959456115029,
                85.054467017581516,
                88.580827542197682,
                92.136175603687079,
                95.719694542143202,
                99.330612454787428,
                102.968198614513810,
                106.631760260643450,
                110.320639714757390,
                114.034211781461690,
                117.771881399745060,
                121.533081515438640,
                125.317271149356880,
                129.123933639127240,
                132.952575035616290,
                136.802722637326350,
                140.673923648234250,
                144.565743946344900,
                148.477766951773020,
                152.409592584497350,
                156.360836303078800,
                160.331128216630930,
                164.320112263195170,
                168.327445448427650,
                172.352797139162820,
                176.395848406997370,
                180.456291417543780,
                184.533828861449510,
                188.628173423671600,
                192.739047287844900,
                196.866181672889980,
                201.009316399281570,
                205.168199482641200,
                209.342586752536820,
                213.532241494563270,
                217.736934113954250,
                221.956441819130360,
                226.190548323727570,
                230.439043565776930,
                234.701723442818260,
                238.978389561834350,
                243.268849002982730,
                247.572914096186910,
                251.890402209723190,
                256.221135550009480,
                260.564940971863220,
                264.921649798552780,
                269.291097651019810,
                273.673124285693690,
                278.067573440366120,
                282.474292687630400,
                286.893133295426990,
                291.323950094270290,
                295.766601350760600,
                300.220948647014100,
                304.686856765668720,
                309.164193580146900,
                313.652829949878990,
                318.152639620209300,
                322.663499126726210,
                327.185287703775200,
                331.717887196928470,
                336.261181979198450,
                340.815058870798960,
                345.379407062266860,
                349.954118040770250,
                354.539085519440790,
                359.134205369575340,
                363.739375555563470,
                368.354496072404690,
                372.979468885689020,
                377.614197873918670,
                382.258588773060010,
                386.912549123217560,
                391.575988217329610,
                396.248817051791490,
                400.930948278915760,
                405.622296161144900,
                410.322776526937280,
                415.032306728249580,
                419.750805599544780,
                424.478193418257090,
                429.214391866651570,
                433.959323995014870,
                438.712914186121170,
                443.475088120918940,
                448.245772745384610,
                453.024896238496130,
                457.812387981278110,
                462.608178526874890,
                467.412199571608080,
                472.224383926980520,
                477.044665492585580,
                481.872979229887900,
                486.709261136839360,
                491.553448223298010,
                496.405478487217580,
                501.265290891579240,
                506.132825342034830,
                511.008022665236070,
                515.890824587822520,
                520.781173716044240,
                525.679013515995050,
                530.584288294433580,
                535.496943180169520,
                540.416924105997740,
                545.344177791154950,
                550.278651724285620,
                555.220294146894960,
                560.169054037273100,
                565.124881094874350,
                570.087725725134190,
                575.057539024710200,
                580.034272767130800,
                585.017879388839220,
                590.008311975617860,
                595.005524249382010,
                600.009470555327430,
                605.020105849423770,
                610.037385686238740,
                615.061266207084940,
                620.091704128477430,
                625.128656730891070,
                630.172081847810200,
                635.221937855059760,
                640.278183660408100,
                645.340778693435030,
                650.409682895655240,
                655.484856710889060,
                660.566261075873510,
                665.653857411105950,
                670.747607611912710,
                675.847474039736880,
                680.953419513637530,
                686.065407301994010,
                691.183401114410800,
                696.307365093814040,
                701.437263808737160,
                706.573062245787470,
                711.714725802289990,
                716.862220279103440,
                722.015511873601330,
                727.174567172815840,
                732.339353146739310,
                737.509837141777440,
                742.685986874351220,
                747.867770424643370,
                753.055156230484160,
                758.248113081374300,
                763.446610112640200,
                768.650616799717000,
                773.860102952558460,
                779.075038710167410,
                784.295394535245690,
                789.521141208958970,
                794.752249825813460,
                799.988691788643450,
                805.230438803703120,
                810.477462875863580,
                815.729736303910160,
                820.987231675937890,
                826.249921864842800,
                831.517780023906310,
                836.790779582469900,
                842.068894241700490,
                847.352097970438420,
                852.640365001133090,
                857.933669825857460,
                863.231987192405430,
                868.535292100464630,
                873.843559797865740,
                879.156765776907600,
                884.474885770751830,
                889.797895749890240,
                895.125771918679900,
                900.458490711945270,
                905.796028791646340,
                911.138363043611210,
                916.485470574328820,
                921.837328707804890,
                927.193914982476710,
                932.555207148186240,
                937.921183163208070,
                943.291821191335660,
                948.667099599019820,
                954.046996952560450,
                959.431492015349480,
                964.820563745165940,
                970.214191291518320,
                975.612353993036210,
                981.015031374908400,
                986.422203146368590,
                991.833849198223450,
                997.249949600427840,
                1002.670484599700300,
                1008.095434617181700,
                1013.524780246136200,
                1018.958502249690200,
                1024.396581558613400,
                1029.838999269135500,
                1035.285736640801600,
                1040.736775094367400,
                1046.192096209724900,
                1051.651681723869200,
                1057.115513528895000,
                1062.583573670030100,
                1068.055844343701400,
                1073.532307895632800,
                1079.012946818975000,
                1084.497743752465600,
                1089.986681478622400,
                1095.479742921962700,
                1100.976911147256000,
                1106.478169357800900,
                1111.983500893733000,
                1117.492889230361000,
                1123.006317976526100,
                1128.523770872990800,
                1134.045231790853000,
                1139.570684729984800,
                1145.100113817496100,
                1150.633503306223700,
                1156.170837573242400,
            };
            return lf[n];
        }
    }
}