# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.6.5-alpha] - 2026-07-24
### New Features
- [`25f1c0f`](https://github.com/CleanroomMC/Cleanroom/commit/25f1c0f0640f9feb0fe6ca58b7ad85aca0489fbe) - **cleanmix**: update to 0.6.0, more effort for backwards compatibility *(commit by [@Rongmario](https://github.com/Rongmario))*

### Bug Fixes
- [`dfd2bca`](https://github.com/CleanroomMC/Cleanroom/commit/dfd2bcaf4154fc660d053dfac38cc86ba7dd1f30) - **discovery**: removed unused mixin configs parameter *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`a4bc796`](https://github.com/CleanroomMC/Cleanroom/commit/a4bc796c0074168f0f72ae8680af251fab716c77) - **discovery**: unable to grab mod ids from certain metadata files *(commit by [@Rongmario](https://github.com/Rongmario))*

### Performance Improvements
- [`044356b`](https://github.com/CleanroomMC/Cleanroom/commit/044356b4d7c2693cf1c97b6c7b74e97885308c6e) - **discovery**: remove additional candidate deduplication check *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`3ac1213`](https://github.com/CleanroomMC/Cleanroom/commit/3ac121329bc156367fc14093bd06f2d5c785afbb) - **discovery**: optimized deduplication checks for mod candidates *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`f35a674`](https://github.com/CleanroomMC/Cleanroom/commit/f35a674d6cb6b24686e9a8e0981f79cef11197e8) - **discovery**: faster deduplication checks when gathering mods *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`6fe1114`](https://github.com/CleanroomMC/Cleanroom/commit/6fe1114f410c22f86e2ad9cdaab9cca2d7d04f19) - **discovery**: walk directory once to find derped installations *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.6.4-alpha] - 2026-07-23
### Bug Fixes
- [`c001d07`](https://github.com/CleanroomMC/Cleanroom/commit/c001d07f9c41fe71347d297e5e6ee0553f4143e4) - **cleanmix**: updated to fix accessors to eagerly load their targets *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.6.3-alpha] - 2026-07-22
### New Features
- [`f0ef797`](https://github.com/CleanroomMC/Cleanroom/commit/f0ef79765c2dc044e07226be41dd527e2c1408ab) - **version check**: add header of cleanroom *(commit by [@baka-gourd](https://github.com/baka-gourd))*

### Bug Fixes
- [`daee859`](https://github.com/CleanroomMC/Cleanroom/commit/daee859456fc067caa1cc0beef1b4827e19e124d) - **cleanmix**: updated to fix various mixin loading bugs *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`64af19c`](https://github.com/CleanroomMC/Cleanroom/commit/64af19c3008c2533aca71260b862d85975baf6dd) - **discovery**: handle directory sources *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.6.2-alpha] - 2026-07-20
### Bug Fixes
- [`7e8bc27`](https://github.com/CleanroomMC/Cleanroom/commit/7e8bc270a3b0fb31161ee64976185fd3d0a0c1a0) - **discovery**: don't parse built-in mods as discovered files *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.6.1-alpha] - 2026-07-20
### Bug Fixes
- [`dcffd58`](https://github.com/CleanroomMC/Cleanroom/commit/dcffd580f565e0c0c310b5204f2241111c9c037e) - **mod-list**: scrollbar sensitive list width *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`568757b`](https://github.com/CleanroomMC/Cleanroom/commit/568757bd70e0813ca983534c2da2e486d1ec11ae) - **mod-list**: consider scrollbar width when trimming text *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`26bc611`](https://github.com/CleanroomMC/Cleanroom/commit/26bc6115d0cc2c41071443c4308a107bad8de7d4) - **mod-list**: set parent via `GuiModList`'s parent *(commit by [@Rongmario](https://github.com/Rongmario))*

### Chores
- [`d7e73ca`](https://github.com/CleanroomMC/Cleanroom/commit/d7e73ca03bb3a41666766552b066de76bd5ee21b) - **cleanmix**: update MixinBooter from 11.2 to 11.5 *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.6.0-alpha] - 2026-07-19
### New Features
- [`ed8e356`](https://github.com/CleanroomMC/Cleanroom/commit/ed8e3564f75eeefa698766eb25ff50c136836960) - **cleanmix**: achieve parity with MixinBooter 11 *(PR [#583](https://github.com/CleanroomMC/Cleanroom/pull/583) by [@Rongmario](https://github.com/Rongmario))*
- [`bd4f552`](https://github.com/CleanroomMC/Cleanroom/commit/bd4f552b80d3089f9e8909a323261da0b1e09aad) - **mod-list**: implemented modern mod list UI *(PR [#589](https://github.com/CleanroomMC/Cleanroom/pull/589) by [@Rongmario](https://github.com/Rongmario))*

### Bug Fixes
- [`69f118c`](https://github.com/CleanroomMC/Cleanroom/commit/69f118cb4abe123d5e3f0a3365623474ff8e751a) - **mod-list**: add `cleanmix` to default catalogue libraries *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`fbf9fc2`](https://github.com/CleanroomMC/Cleanroom/commit/fbf9fc255ed3940d0801d16b8416bd4ed9b6daaa) - **patch-mods**: check if patch mods are present outside of dev *(commit by [@Rongmario](https://github.com/Rongmario))*
- [`e01a298`](https://github.com/CleanroomMC/Cleanroom/commit/e01a29831376f15fa8f993605a81cf135514dacd) - vanilla json using broken objc-bridge *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Performance Improvements
- [`6b8c126`](https://github.com/CleanroomMC/Cleanroom/commit/6b8c1260fec2af78aa1ed353e720ef8e3a61511e) - **discovery**: speed up `ASMDataTable::getAnnotationsFor` (O(n^2) -> O(n)) *(PR [#582](https://github.com/CleanroomMC/Cleanroom/pull/582) by [@romanfedyniak](https://github.com/romanfedyniak))*

### Refactors
- [`a476dbe`](https://github.com/CleanroomMC/Cleanroom/commit/a476dbe805853de6af764e2d85cd66beb2303532) - **mod-list**: re-situate related event listeners *(commit by [@Rongmario](https://github.com/Rongmario))*


## [0.5.17-alpha] - 2026-07-09
### Bug Fixes
- [`a436b61`](https://github.com/CleanroomMC/Cleanroom/commit/a436b61ad97099497ac8ebda48819bb66d833d86) - installer can't install server *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.16-alpha] - 2026-07-09
### New Features
- [`db96747`](https://github.com/CleanroomMC/Cleanroom/commit/db967479f558d66f3121259b82056a6a044b3258) - New installer and client.json compat w/ official launcher *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`0fff847`](https://github.com/CleanroomMC/Cleanroom/commit/0fff847600fcca3a3fd5d1a95e9aa57230233000) - allow flash can be disabled *(commit by [@baka-gourd](https://github.com/baka-gourd))*
- [`83ea6a4`](https://github.com/CleanroomMC/Cleanroom/commit/83ea6a4b820d732f5aa6fde122a03a74002fa1c4) - early config now available in mod config menu *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Bug Fixes
- [`c0e60dc`](https://github.com/CleanroomMC/Cleanroom/commit/c0e60dcebd3960fb871a147779d82ed78bbdf591) - fugue warning sometimes hang the game after pressed any key *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`b027c41`](https://github.com/CleanroomMC/Cleanroom/commit/b027c41715dedad0f5d7e407aafbaa2478cd1bef) - ignore relation of all built-in mods *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Chores
- [`0d9caab`](https://github.com/CleanroomMC/Cleanroom/commit/0d9caab6d5547d86feb397872e1b39328fdcfd37) - actually call translationkey funcs in Fluid *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.15-alpha] - 2026-06-29
### New Features
- [`b5abee9`](https://github.com/CleanroomMC/Cleanroom/commit/b5abee957b17fa405f99217ff9303ce60387bde1) - Warn users if Fugue or Scalar not installed *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`e545c03`](https://github.com/CleanroomMC/Cleanroom/commit/e545c037760efb34ed029834262348749810ce52) - Warn if Fugue or Scalar not installed *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Bug Fixes
- [`8789256`](https://github.com/CleanroomMC/Cleanroom/commit/87892564cb7763f3e512d450ed2ed5c5bf914eb7) - Not wrans right after coremod *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Chores
- [`c7f8e4c`](https://github.com/CleanroomMC/Cleanroom/commit/c7f8e4c206495c342b087a49103161507d628750) - Improve warning *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`6c09515`](https://github.com/CleanroomMC/Cleanroom/commit/6c09515e472be5583e418a614ac8874001b2ff3b) - update early config about framebuffer *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`e4aa30a`](https://github.com/CleanroomMC/Cleanroom/commit/e4aa30a8dcad341825ab4de90b03e3acf70f9dde) - Correct colon in zh_cn.lang *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`ddefbb7`](https://github.com/CleanroomMC/Cleanroom/commit/ddefbb72171261f5c7bb725cfc1d702e092c033a) - Fluid method names that fit latest mcp *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.14-alpha] - 2026-06-10
### Bug Fixes
- [`43bcf47`](https://github.com/CleanroomMC/Cleanroom/commit/43bcf475eb350be2ea6b9b8e10153d8ecbabe29e) - userdev jar is broken *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.13-alpha] - 2026-06-10
### Bug Fixes
- [`8843e7b`](https://github.com/CleanroomMC/Cleanroom/commit/8843e7b991f3ce08a555d116469fe51fdcc08ec0) - Can't discover in-dev mod when multi-langs *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.12-alpha] - 2026-05-09
### Bug Fixes
- [`0058f2f`](https://github.com/CleanroomMC/Cleanroom/commit/0058f2fb9092c6909ca5409da1ca88dd11059f92) - lwjglxx and OpenGlHelper not using new oshi path *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.11-alpha] - 2026-05-09
### Bug Fixes
- [`ecdd87b`](https://github.com/CleanroomMC/Cleanroom/commit/ecdd87bd33e2eb870bc264dd0407c4533450051e) - oshi now divided into two packages *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*

### Other Changes
- [`3d5d7df`](https://github.com/CleanroomMC/Cleanroom/commit/3d5d7dfb4d6bfb7fb679702eaf18b2903d0b0419) - Merge branch 'main' of github.com:CleanroomMC/Cleanroom *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.10-alpha] - 2026-05-09
### Bug Fixes
- [`06824ad`](https://github.com/CleanroomMC/Cleanroom/commit/06824ad4e9761d60b52b2c25b3345d4947130a5c) - typesetting in FontRenderer *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.9-alpha] - 2026-04-30
### New Features
- [`4a76d2f`](https://github.com/CleanroomMC/Cleanroom/commit/4a76d2f5b9ad89d3a786a95d2b56f263b9df6716) - aggresive loading tracker *(PR [#553](https://github.com/CleanroomMC/Cleanroom/pull/553) by [@baka-gourd](https://github.com/baka-gourd))*

### Other Changes
- [`e238bed`](https://github.com/CleanroomMC/Cleanroom/commit/e238bed837942573424ab12c02210afdec32ef59) - Update mixin extra *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`266851d`](https://github.com/CleanroomMC/Cleanroom/commit/266851da7d04c77e7f4373d2b4300fd78be66de7) - Merge branch 'main' of github.com:CleanroomMC/Cleanroom *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`7be307d`](https://github.com/CleanroomMC/Cleanroom/commit/7be307dc2ad50c2f8bb1e91a1360d3728dd4aea6) - Uncomment objc bridge *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.8-alpha] - 2026-04-08
### Other Changes
- [`2a723e2`](https://github.com/CleanroomMC/Cleanroom/commit/2a723e2d47b6607942c3f209de364e716dadb4c6) - Update foundation *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`d027e4d`](https://github.com/CleanroomMC/Cleanroom/commit/d027e4d6a54eb33d779ebf13be8a5861525c16f1) - Update jline *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`1ebbe41`](https://github.com/CleanroomMC/Cleanroom/commit/1ebbe4193afb953350e1d231741f93c18241d60c) - Switch out from imaginebreaker *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`989612d`](https://github.com/CleanroomMC/Cleanroom/commit/989612dfa7333f16180cd7310c53b2b46cc3dc69) - Update deps *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`5ef9cf9`](https://github.com/CleanroomMC/Cleanroom/commit/5ef9cf9b093bea2934a3b6f5dfa432d5be952df1) - Update Foundation *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`173a914`](https://github.com/CleanroomMC/Cleanroom/commit/173a914ff4fad253d392c26d2d4253b5860c2a08) - Add dep for Reflect *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*
- [`f100bc8`](https://github.com/CleanroomMC/Cleanroom/commit/f100bc88b83daca1337c753a2a0038e48bb3a8a0) - Update Reflect, add vararg enum test *(commit by [@kappa-maintainer](https://github.com/kappa-maintainer))*


## [0.5.7-alpha] - 2026-03-31
### New Features
- [`7646e15`](https://github.com/CleanroomMC/Cleanroom/commit/7646e157d6937272d9374a0c4c90dc222e5d4002) - add progressbar config *(commit by [@baka-gourd](https://github.com/baka-gourd))*
- [`8acf305`](https://github.com/CleanroomMC/Cleanroom/commit/8acf305102d8ed7435b223a11f36dee93c9a6065) - add subprogress *(commit by [@baka-gourd](https://github.com/baka-gourd))*
- [`bc98b18`](https://github.com/CleanroomMC/Cleanroom/commit/bc98b1889a06ca43413281d51b0c7335338e57c2) - truly progress counting *(commit by [@baka-gourd](https://github.com/baka-gourd))*
- [`be42de4`](https://github.com/CleanroomMC/Cleanroom/commit/be42de4b6565f2c713d88c962f430dd92cec9d91) - more defensive programming, and add more log *(commit by [@baka-gourd](https://github.com/baka-gourd))*

[0.5.7-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.6-alpha...0.5.7-alpha
[0.5.8-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.7-alpha...0.5.8-alpha
[0.5.9-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.8-alpha...0.5.9-alpha
[0.5.10-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.9-alpha...0.5.10-alpha
[0.5.11-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.10-alpha...0.5.11-alpha
[0.5.12-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.11-alpha...0.5.12-alpha
[0.5.13-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.12-alpha...0.5.13-alpha
[0.5.14-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.13-alpha...0.5.14-alpha
[0.5.15-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.14-alpha...0.5.15-alpha
[0.5.16-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.15-alpha...0.5.16-alpha
[0.5.17-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.16-alpha...0.5.17-alpha
[0.6.0-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.5.17-alpha...0.6.0-alpha
[0.6.1-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.6.0-alpha...0.6.1-alpha
[0.6.2-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.6.1-alpha...0.6.2-alpha
[0.6.3-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.6.2-alpha...0.6.3-alpha
[0.6.4-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.6.3-alpha...0.6.4-alpha
[0.6.5-alpha]: https://github.com/CleanroomMC/Cleanroom/compare/0.6.4-alpha...0.6.5-alpha
