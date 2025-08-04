class DependencyBuilder:
    def __init__(self, artifact: dict = None, name: str = None, rules: list = None):
        self.builder = {}
        if artifact:
            self.builder['downloads'] = artifact
        if name:
            self.builder['name'] = name
        if rules:
            self.builder['rules'] = rules

    def set_artifact(self, artifact: dict):
        if not skip_pop_if_exist(self.builder, 'downloads', artifact):
            self.builder['downloads'] = artifact
        return self

    def get_artifact(self) -> dict:
        return self.builder['downloads']

    def set_name(self, name: str):
        if not skip_pop_if_exist(self.builder, 'name', name):
            self.builder['name'] = name
        return self

    def get_name(self) -> str:
        return self.builder['name']

    def set_mmc_hint(self, mmc_hint: str):
        if not skip_pop_if_exist(self.builder, 'MMC-hint', mmc_hint):
            real_hint = False
            for exist_rule in ['local', 'always-stale', 'forge-pack-xz']:
                if mmc_hint is exist_rule:
                    real_hint = True
            if not real_hint:
                raise Exception('Rule used not exist')
            self.builder['MMC-hint'] = mmc_hint
        return self

    def get_mmc_hint(self) -> str:
        return self.builder['MMC-hint']

    def set_rules(self, rules: list):
        if not skip_pop_if_exist(self.builder, 'rules', rules):
            self.builder['rules'] = rules
        return self

    def get_rules(self) -> list:
        return self.builder['rules']

    def build(self) -> dict:
        verify(self.builder, 'name')
        if not self.builder['MMC-hint'] or self.builder['MMC-hint'] != 'local':
            verify(self.builder, 'downloads')
            verify(self.builder['downloads']['artifact'], 'sha1', 'size', 'url')
        return self.builder


class ArtifactBuilder:
    def __init__(self, sha1: str = None, size: str = None, url: str = None):
        self.builder = {}
        if sha1:
            self.builder['sha1'] = sha1
        if size:
            self.builder['size'] = size
        if url:
            self.builder['url'] = url

    def set_sha1(self, sha1: str):
        if not skip_pop_if_exist(self.builder, 'sha1', sha1):
            self.builder['sha1'] = sha1
        return self

    def get_sha1(self) -> str:
        return self.builder['sha1']

    def set_size(self, size: str):
        if not skip_pop_if_exist(self.builder, 'size', size):
            self.builder['size'] = size
        return self

    def get_size(self) -> str:
        return self.builder['size']

    def set_url(self, url: str):
        if not skip_pop_if_exist(self.builder, 'url', url):
            self.builder['url'] = url
        return self

    def get_url(self) -> str:
        return self.builder['url']

    def build(self) -> dict:
        verify(self.builder, 'sha1', 'size', 'url')
        return {'artifact': self.builder}


class RulesBuilder:
    def __init__(self):
        self.builder = []

    def append_rule(self, action: str = None, os_name: str = None):
        assert action
        temp = {'action': action}
        if os_name:
            temp['os']['name'] = os_name
        self.builder.append(temp)
        return self

    def pop_rule(self, rule_id):
        assert rule_id
        if self.builder[rule_id]:
            self.builder.pop(rule_id)
        return self

    def get_rule(self, index: int) -> dict:
        return self.builder[index]

    def build(self) -> list:
        if self.builder:
            for rule in self.builder:
                verify(rule, 'action')
        return self.builder


def verify(dict, *args):
    for arg in args:
        assert dict[arg]


def skip_pop_if_exist(dict, key: str = None, value=None):
    if not value:
        assert key
        if dict[key]:
            dict(key)
